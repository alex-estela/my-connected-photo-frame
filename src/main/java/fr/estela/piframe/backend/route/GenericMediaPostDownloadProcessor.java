package fr.estela.piframe.backend.route;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fr.estela.piframe.backend.entity.MediaEntity;
import fr.estela.piframe.backend.entity.ProviderEntity;
import fr.estela.piframe.backend.repository.MediaRepository;

@Component
public class GenericMediaPostDownloadProcessor implements Processor {

	@Autowired
	private Logger logger;
	
	@Autowired
	private MediaRepository mediaRepository;
	
	@Override
	public void process(Exchange exchange) throws Exception {
		
		// ProviderEntity providerEntity = (ProviderEntity) exchange.getProperty("providerEntity");

		// MediaEntity mediaEntity = (MediaEntity) exchange.getIn().getBody();
		
		//mediaEntity.setProvider(providerEntity);
	    //mediaEntity.setMediaState(MediaState.REMOTE);
		
		//mediaRepository.save(mediaEntity);
		
		// logger.info("Saved remote " + mediaEntity.getRemoteId() + " to local " + mediaEntity.getId());
	}
}