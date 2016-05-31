package fr.estela.piframe.backend.api.v1.controller;

import javax.ws.rs.core.MediaType;

import org.springframework.stereotype.Controller;

import io.swagger.inflector.models.RequestContext;
import io.swagger.inflector.models.ResponseContext;

@Controller
public class ToolingController extends AbstractController {
	
    public ResponseContext toolsPingGET(RequestContext request) {		
		ResponseContext response = new ResponseContext();
		response.setEntity("1");	
		response.setContentType(MediaType.APPLICATION_JSON_TYPE);
		return response;
	}
}