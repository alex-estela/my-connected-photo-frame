package fr.estela.piframe.backend.route;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fr.estela.piframe.backend.entity.ProviderEntity;
import fr.estela.piframe.backend.repository.ProviderRepository;

@Component
public class GenericProviderPostDownloadProcessor implements Processor {

	@Autowired
	private Logger logger;
	
	@Autowired
	private ProviderRepository providerRepository;
	
	@Override
	public void process(Exchange exchange) throws Exception {
		
		ProviderEntity providerEntity = (ProviderEntity) exchange.getProperty("providerEntity");
		PaginatedDownloadConfig paginatedDownloadConfig = (PaginatedDownloadConfig) exchange.getProperty("paginatedDownloadConfig");
		
		if (paginatedDownloadConfig.isProviderUpdated()) {
		
			providerEntity = providerRepository.save(providerEntity);	
		
			exchange.setProperty("providerEntity", providerEntity);
			
			paginatedDownloadConfig.setProviderUpdated(false);
			
			logger.debug("> saved providerEntity with id #" + providerEntity.getId() 
			+ ", lastPollToken=" + providerEntity.getLastPollToken());
		}
	}
}