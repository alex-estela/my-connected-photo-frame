package fr.estela.piframe.backend.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import fr.estela.piframe.backend.entity.MediaEntity;
import fr.estela.piframe.backend.repository.MediaRepository;
import fr.estela.piframe.backend.util.FileUtils;

@RestController
public class MediaAPI {

	@Autowired
	private Logger logger;
	
	@Autowired
	private MediaRepository mediaRepository;
	
	@CrossOrigin
	@RequestMapping(value = "/ping", method = RequestMethod.GET, produces="application/json")
	public int ping() throws Exception {
		return 1;
	}
	
	@CrossOrigin
	@RequestMapping(value = "/medias", method = RequestMethod.GET, produces="application/json")
	public List<MediaResource> getMedias(@RequestParam(name="random", required=false) Boolean random) throws Exception {
		
		List<MediaEntity> mediaEntities = mediaRepository.findAll();
		
		List<MediaResource> mediaResources = new ArrayList<MediaResource>();
		
		for (MediaEntity mediaEntity : mediaEntities) {
			
			String uri = "/localcontent/medias/";
			uri += mediaEntity.getId() + "." + FileUtils.getMediaFileExtension(mediaEntity.getMediaType());
			
			MediaResource mediaResource = new MediaResource();
			mediaResource.setLocalContentURI(uri);
			mediaResource.setLocalWidth(mediaEntity.getLocalContent().getWidth());
			mediaResource.setLocalHeight(mediaEntity.getLocalContent().getHeight());
			
			mediaResources.add(mediaResource);
		}
		
		if (random != null && random) Collections.shuffle(mediaResources);
		
		return mediaResources;
	}
	
	@CrossOrigin
	@RequestMapping(value = "/localcontent/medias/{mediaId}.{mediaFileExtension}", method = RequestMethod.GET)
	public HttpEntity<byte[]> getMediaLocalContent(@PathVariable("mediaId") String mediaId) throws Exception {
		
		MediaEntity mediaEntity = mediaRepository.findOne(UUID.fromString(mediaId));
		
		if (mediaEntity == null) {
			logger.error("Media not found for download:" + mediaId);
			return null;
		}
		
		//if (mediaEntity.getMediaState() != MediaState.LOCAL) {
			//logger.error("Media not in LOCAL state, instead it is:" + mediaEntity.getMediaState());
			//return null;
		//}
		
		byte[] bytes = mediaEntity.getLocalContent().getContentStream().getBytes();
		
	    HttpHeaders header = new HttpHeaders();
	    header.setContentType(FileUtils.getMediaContentType(mediaEntity.getMediaType()));
	    header.set("Content-Disposition", "attachment; filename=" + mediaId + "." + FileUtils.getMediaFileExtension(mediaEntity.getMediaType()));
	    header.setContentLength(bytes.length);

	    return new HttpEntity<byte[]>(bytes, header);
	}
}
