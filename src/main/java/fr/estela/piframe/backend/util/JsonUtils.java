package fr.estela.piframe.backend.util;

import org.slf4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonUtils {

	public static void printPrettyJson(Logger logger, String json) {
		try {
		    ObjectMapper mapper = new ObjectMapper();
		    logger.info(
		    	mapper.writerWithDefaultPrettyPrinter().
		    	writeValueAsString(mapper.readValue(json, Object.class)));			
		}
		catch (Exception e) {
			e.printStackTrace();
		}	    
	}
	
	public static boolean jsonNodeExists(JsonNode node) {
		return node != null && !node.isMissingNode() && !node.isNull();
	}
}
