package fr.estela.piframe.backend.route;

import java.util.List;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fr.estela.piframe.backend.entity.MediaEntity;
import fr.estela.piframe.backend.entity.ProviderEntity;
import fr.estela.piframe.backend.repository.ProviderRepository;

@Component
public abstract class AbstractDownloadProcessor implements Processor {
	
	@Autowired
	private ProviderRepository providerRepository;

	@Override
	public void process(Exchange exchange) throws Exception {
		
		Long providerEntityId = (Long) exchange.getProperty("providerEntityId");
		ProviderEntity providerEntity = providerRepository.findOne(providerEntityId);
		
		PaginatedDownloadConfig paginatedDownloadConfig = (PaginatedDownloadConfig) exchange.getProperty("paginatedDownloadConfig");
		if (paginatedDownloadConfig == null) paginatedDownloadConfig = new PaginatedDownloadConfig();
		
		List<MediaEntity> medias = downloadAndInstanciateMedias(providerEntity, paginatedDownloadConfig);
		
		exchange.getIn().setBody(medias, List.class);
		exchange.setProperty("providerEntity", providerEntity);
		exchange.setProperty("paginatedDownloadConfig", paginatedDownloadConfig);
	}
	
	/**
	 * The downloadAndInstanciateMedias method is responsible for downloading, instanciating and returning images, videos and other medias.
	 * Each returned MediaEntity shall later be inserted (or eventually updated) in the proper JPA repository.
	 * Note that providerEntity attributes and paginatedDownloadConfig attributes can be updated if necessary (e.g. lastPollToken, lastPageDownloaded).
	 * @param providerEntity
	 * @param paginatedDownloadConfig
	 * @return List<MediaEntity>
	 */
	public abstract List<MediaEntity> downloadAndInstanciateMedias(ProviderEntity providerEntity, PaginatedDownloadConfig paginatedDownloadConfig) throws Exception;
}
