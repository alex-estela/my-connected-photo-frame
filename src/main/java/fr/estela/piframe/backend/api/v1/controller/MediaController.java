package fr.estela.piframe.backend.api.v1.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;

import fr.estela.piframe.backend.api.v1.model.Media;
import fr.estela.piframe.backend.entity.MediaEntity;
import fr.estela.piframe.backend.repository.MediaRepository;
import io.swagger.inflector.models.RequestContext;
import io.swagger.inflector.models.ResponseContext;

@Controller @Transactional
public class MediaController extends AbstractController {
	
	@Autowired
	private MediaRepository mediaRepository;

	public ResponseContext mediasGET(RequestContext request, Boolean random) {

		List<MediaEntity> mediaEntities = mediaRepository.findAll();
		List<Media> medias = new ArrayList<Media>();
		
		for (MediaEntity mediaEntity : mediaEntities) {
			
			Media media = new Media();
			media.setId(mediaEntity.getId().toString());
			media.setMediaType(mediaEntity.getMediaType());
			media.setWidth(mediaEntity.getLocalContent().getWidth());
			media.setHeight(mediaEntity.getLocalContent().getHeight());
			media.setCreated(mediaEntity.getOriginallyCreated());
			
			medias.add(media);
		}
		
		if (random != null && random) Collections.shuffle(medias);

		ResponseContext response = new ResponseContext();
		response.setEntity(medias);
		response.setContentType(MediaType.APPLICATION_JSON_TYPE);
		return response;
	}
	
    public ResponseContext mediasMediaIdGET(RequestContext request, String mediaId) {
    	
    	if (request.getAcceptableMediaTypes() == null || request.getAcceptableMediaTypes().isEmpty())
    		throw newApiException("No media type specified");
    	
    	String requestMediaType = request.getAcceptableMediaTypes().get(0).getSubtype().trim().toLowerCase();
    	ResponseContext response = new ResponseContext();
    	
    	MediaEntity mediaEntity = mediaRepository.findOne(UUID.fromString(mediaId));
    	
    	if (requestMediaType.equals("json") || requestMediaType.equals("html")) {
    		
			Media media = new Media();
			media.setId(mediaEntity.getId().toString());
			media.setMediaType(mediaEntity.getMediaType());
			media.setWidth(mediaEntity.getLocalContent().getWidth());
			media.setHeight(mediaEntity.getLocalContent().getHeight());
			media.setCreated(mediaEntity.getOriginallyCreated());
    		
    		response.setContentType(MediaType.APPLICATION_JSON_TYPE);
    		response.setEntity(media);
    	}
    	//else if (requestMediaType.equals("webp") || requestMediaType.equals("jpeg") 
    	//	|| requestMediaType.equals("gif") || requestMediaType.equals("png")) {
    	else {
    		
    		byte[] bytes = mediaEntity.getLocalContent().getContentStream().getBytes();
    		
    		MultivaluedMap<String, String> headers = new MultivaluedHashMap<String, String>();
    	    //headers.add("Content-Disposition", "attachment; filename=" + mediaEntity + "." + requestMediaType);
    	    headers.add("Content-Length", ""+bytes.length);
    	    
    	    response.setEntity(bytes);
    		response.setContentType(new MediaType("image", requestMediaType));
    		response.setHeaders(headers);
    	}
    	//else throw newApiException("Invalid media type: " + requestMediaType);
    	
		return response;
    }
}