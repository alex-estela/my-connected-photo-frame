package fr.estela.piframe.backend.route;

import java.util.Map;

import org.apache.camel.Properties;
import org.springframework.stereotype.Component;

@Component
public class PaginatedDownloadRouter {

	public String route(@Properties Map<String, Object> properties) {
		
		PaginatedDownloadConfig paginatedDownloadConfig = (PaginatedDownloadConfig) properties.get("paginatedDownloadConfig");
		
		if (paginatedDownloadConfig != null && paginatedDownloadConfig.isLastPageDownloaded()) {
			return null;
		}
		else {
			String providerRouteId = (String) properties.get("providerRouteId");
			return providerRouteId;
		}
	}
}