package fr.estela.piframe.backend.sourcepack.smugmug;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.auth.oauth.OAuthHmacSigner;
import com.google.api.client.auth.oauth.OAuthParameters;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.javanet.NetHttpTransport;

import fr.estela.piframe.backend.entity.MediaContentEntity;
import fr.estela.piframe.backend.entity.MediaContentStreamEntity;
import fr.estela.piframe.backend.entity.MediaEntity;
import fr.estela.piframe.backend.entity.ProviderEntity;
import fr.estela.piframe.backend.repository.MediaRepository;
import fr.estela.piframe.backend.route.AbstractDownloadProcessor;
import fr.estela.piframe.backend.route.PaginatedDownloadConfig;
import fr.estela.piframe.backend.util.JsonUtils;
import fr.estela.piframe.backend.util.MediaType;
import fr.estela.piframe.backend.util.StreamGobbler;

@Component
public class SmugmugDownloadProcessor extends AbstractDownloadProcessor {
	
	@Autowired
	private Logger logger;
	
	@Autowired
	private MediaRepository mediaRepository;
	
	@Override
	public List<MediaEntity> downloadAndInstanciateMedias(ProviderEntity providerEntity, PaginatedDownloadConfig paginatedDownloadConfig) throws Exception {
		
		logger.info("Smugmug download initiated...");
		
		SmugmugProviderEntity smugmugProviderEntity = (SmugmugProviderEntity) providerEntity;
		int pageSize = 10;
		
		List<MediaEntity> medias = new ArrayList<MediaEntity>();
		
	    OAuthHmacSigner signer = new OAuthHmacSigner();
	    signer.clientSharedSecret = smugmugProviderEntity.getConsumerSecret();
	    signer.tokenSharedSecret = smugmugProviderEntity.getAccessTokenSecret();
	    
	    OAuthParameters oauthParameters = new OAuthParameters();
	    oauthParameters.signer = signer;
	    oauthParameters.consumerKey = smugmugProviderEntity.getConsumerKey();
	    oauthParameters.token = smugmugProviderEntity.getAccessToken();
	    
	    HttpRequestFactory requestFactory = new NetHttpTransport().createRequestFactory(oauthParameters);
	    
	    HttpHeaders headers = new HttpHeaders();
	    headers.setAccept("application/json");	
	    
		logger.debug("> Smugmug album: " + smugmugProviderEntity.getAlbum());
		logger.debug("> Smugmug pageIndex: " + paginatedDownloadConfig.getPageIndex());
	    
	    GenericUrl downloadUrl = new GenericUrl("https://api.smugmug.com/api/v2/album/" + smugmugProviderEntity.getAlbum() 
	    	+ "!images?start=" + paginatedDownloadConfig.getPageIndex() + "&count=" + pageSize);	    
	    HttpRequest downloadRequest = requestFactory.buildGetRequest(downloadUrl);
	    downloadRequest.setHeaders(headers);
	    HttpResponse downloadResponse = downloadRequest.execute();
	    String json = downloadResponse.parseAsString();
	    downloadResponse.getContent().close();
	    //JsonUtils.printPrettyJson(logger, json);
	    
		logger.debug("> Smugmug response received");
	    
	    ObjectMapper mapper = new ObjectMapper();	    
		JsonNode rootNode = mapper.readTree(json);
		
		JsonNode responseNode = rootNode.path("Response");
		if (!JsonUtils.jsonNodeExists(responseNode)) {
			throw new Exception("Download error for SmugmugProviderEntity #" + smugmugProviderEntity.getId() + ": missing Response node");
		}
		
		JsonNode albumImageNode = responseNode.path("AlbumImage");
		if (!JsonUtils.jsonNodeExists(albumImageNode)) {
			logger.info("Ending Smugmug downloads at pageIndex " + paginatedDownloadConfig.getPageIndex());
			paginatedDownloadConfig.setLastPageDownloaded(true);
			return medias;
		}

		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX");
		
		for (Iterator<JsonNode> i1 = albumImageNode.elements(); i1.hasNext();) {
			
			JsonNode fileNode = i1.next();
			
			String remoteId = fileNode.get("ImageKey").textValue();
			Date lastUpdated = dateFormat.parse(fileNode.get("LastUpdated").textValue());
			String type = fileNode.get("Format").textValue().trim().toLowerCase();
			int remoteWidth = fileNode.get("OriginalWidth").intValue();
			int remoteHeight = fileNode.get("OriginalHeight").intValue();
			Date originallyCreated = dateFormat.parse(fileNode.get("Date").textValue());
			
			MediaEntity mediaEntity = mediaRepository.findByRemoteId(remoteId);
			if (mediaEntity == null) mediaEntity = new MediaEntity();
			
			if (mediaEntity.getLastUpdated() == null || !dateFormat.format(mediaEntity.getLastUpdated()).equals(dateFormat.format(lastUpdated))) {
				
			    GenericUrl contentUrl = new GenericUrl(fileNode.get("ArchivedUri").textValue());
			    HttpRequest contentRequest = requestFactory.buildGetRequest(contentUrl);
			    HttpResponse contentResponse = contentRequest.execute();
			    InputStream contentStream = contentResponse.getContent();
			    
			    UUID mediaId = UUID.randomUUID();
				String originalPath = System.getProperty("user.home") + "/piframe/tmp/" + mediaId + "-original";
				String convertedPath = System.getProperty("user.home") + "/piframe/tmp/" + mediaId + "-converted";
				Path originalPathObj = Paths.get(originalPath);
				Path convertedPathObj = Paths.get(convertedPath);

				logger.debug("> writing to " + originalPath);
				Files.copy(contentStream, originalPathObj);
			    contentStream.close();
			    
				String cmd = "imagemagick-convert -resize x800 " + originalPath + " " + convertedPath;		
				logger.debug("> converting to " + convertedPath);
				logger.debug(cmd);
				
				Process process = Runtime.getRuntime().exec(cmd);
				StreamGobbler errorGobbler = new StreamGobbler(process.getErrorStream(), logger, "CONVERT-ERROR");
				errorGobbler.start();
				StreamGobbler inputGobbler = new StreamGobbler(process.getInputStream(), logger, "CONVERT-INPUT");
				inputGobbler.start();
				errorGobbler.join();
				inputGobbler.join();
				process.getOutputStream().close();
				process.waitFor();
				process.destroy();

				logger.debug("> reading from " + convertedPath);
				
				byte[] localBytes = Files.readAllBytes(convertedPathObj);
				
				MediaContentStreamEntity localMediaContentStreamEntity = new MediaContentStreamEntity();
				localMediaContentStreamEntity.setBytes(localBytes);
				
			    MediaContentEntity localMediaContentEntity = new MediaContentEntity();
			    localMediaContentEntity.setContentStream(localMediaContentStreamEntity);
			    localMediaContentEntity.setWidth(remoteWidth); // TODO	
			    localMediaContentEntity.setHeight(remoteHeight); // TODO
				mediaEntity.setLocalContent(localMediaContentEntity);
				
			    MediaContentEntity remoteMediaContentEntity = new MediaContentEntity();
			    remoteMediaContentEntity.setWidth(remoteWidth);			
			    remoteMediaContentEntity.setHeight(remoteHeight);			
				mediaEntity.setRemoteContent(remoteMediaContentEntity);
			    
			    
			    
			    

				Files.delete(Paths.get(originalPath));
				Files.delete(Paths.get(convertedPath));
				
	
			    
			    if (mediaEntity.getRemoteId() == null) mediaEntity.setRemoteId(remoteId);
				mediaEntity.setMediaType(getMediaTypeFromJsonValue(type));
				mediaEntity.setOriginallyCreated(originallyCreated);
				mediaEntity.setLastUpdated(lastUpdated);
			    
				medias.add(mediaEntity);
				
				logger.debug("> downloaded Smugmug " + mediaEntity.getMediaType() + " " + mediaEntity.getRemoteId());	
				
				mediaEntity.setId(mediaId);
				mediaEntity.setProvider(providerEntity);
			    //mediaEntity.setMediaState(MediaState.REMOTE);
				
				mediaRepository.save(mediaEntity);
				
				logger.info("Saved remote " + mediaEntity.getRemoteId() + " to local " + mediaEntity.getId());
			}
		}
		
		paginatedDownloadConfig.setLastPageDownloaded(false);
		paginatedDownloadConfig.setPageIndex(paginatedDownloadConfig.getPageIndex() + pageSize);
		
		logger.info("Downloaded " + medias.size() + " Smugmug medias");
	    
		return medias;
	}
	
	private MediaType getMediaTypeFromJsonValue(String value) {
		if (value.equals("jpg") || value.equals("jpeg")) return MediaType.JPG;
		logger.warn("Unknown media content type: " + value);
		return null;
	}
}