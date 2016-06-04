package fr.estela.piframe.backend.api.v1.controller;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.MediaType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;

import fr.estela.piframe.backend.api.v1.model.Provider;
import fr.estela.piframe.backend.api.v1.model.Provider.ProviderModelEnum;
import fr.estela.piframe.backend.api.v1.model.SmugmugProvider;
import fr.estela.piframe.backend.entity.ProviderEntity;
import fr.estela.piframe.backend.repository.ProviderRepository;
import fr.estela.piframe.backend.source.smugmug.SmugmugProviderEntity;
import io.swagger.inflector.models.RequestContext;
import io.swagger.inflector.models.ResponseContext;

@Controller @Transactional
public class ProviderController extends AbstractController {
	
	@Autowired
	private ProviderRepository providerRepository;

	private Provider toProvider(ProviderEntity providerEntity) {

		Provider provider = null;
		if (providerEntity instanceof SmugmugProviderEntity) {
			SmugmugProviderEntity smugmugProviderEntity = (SmugmugProviderEntity) providerEntity;
			SmugmugProvider smugmugProvider = new SmugmugProvider();
			smugmugProvider.setProviderModel(ProviderModelEnum.SMUGMUGPROVIDER);
			smugmugProvider.setAlbum(smugmugProviderEntity.getAlbum());
			provider = smugmugProvider;
		}
		else throw newApiException("Unsupported provider type: " + providerEntity);
		
		provider.setId(providerEntity.getId());
		provider.setName(providerEntity.getName());
		provider.setConsumerKey(providerEntity.getConsumerKey());
		provider.setConsumerSecret(providerEntity.getConsumerSecret());
		provider.setAccessToken(providerEntity.getAccessToken());
		provider.setAccessTokenSecret(providerEntity.getAccessTokenSecret());
		return provider;
	}

	private ProviderEntity toProviderEntity(Provider provider) {
		
		ProviderEntity providerEntity = null;
		if (provider instanceof SmugmugProvider) {
			SmugmugProvider smugmugProvider = (SmugmugProvider) provider;
			SmugmugProviderEntity smugmugProviderEntity = new SmugmugProviderEntity();
			smugmugProviderEntity.setAlbum(smugmugProvider.getAlbum());
			providerEntity = smugmugProviderEntity;
		}
		else throw newApiException("Unsupported provider type: " + provider);
		
		providerEntity.setId(provider.getId());
		providerEntity.setName(provider.getName());
		providerEntity.setConsumerKey(provider.getConsumerKey());
		providerEntity.setConsumerSecret(provider.getConsumerSecret());
		providerEntity.setAccessToken(provider.getAccessToken());
		providerEntity.setAccessTokenSecret(provider.getAccessTokenSecret());
		return providerEntity;
	}
	
    public ResponseContext providersGET(RequestContext request) {
    	
    	List<ProviderEntity> providerEntities = providerRepository.findAll();
		List<Provider> providers = new ArrayList<Provider>();		
		for (ProviderEntity providerEntity : providerEntities) {
			Provider provider = toProvider(providerEntity);
			providers.add(provider);
		}
		
		ResponseContext response = new ResponseContext();
		response.setEntity(providers);
		response.setContentType(MediaType.APPLICATION_JSON_TYPE);
		return response;
    }

	public ResponseContext providersPOST(RequestContext request, Provider provider) {
		
		ProviderEntity providerEntity = toProviderEntity(provider);		
		providerEntity = providerRepository.save(providerEntity);
		provider.setId(providerEntity.getId());
		
		ResponseContext response = new ResponseContext();
		response.setEntity(provider);	
		response.setContentType(MediaType.APPLICATION_JSON_TYPE);
		return response;
	}

    public ResponseContext providersProviderIdPUT(RequestContext request, Long providerId, Provider provider) {
    	
    	ProviderEntity providerEntity = toProviderEntity(provider);
    	providerEntity.setId(providerId);
		providerRepository.save(providerEntity);
		
		ResponseContext response = new ResponseContext();
		response.setEntity(provider);	
		response.setContentType(MediaType.APPLICATION_JSON_TYPE);
		return response;
    }
	
    public ResponseContext providersProviderIdDELETE(RequestContext request, Long providerId) {
    	
		providerRepository.delete(providerId);
		
		ResponseContext response = new ResponseContext();
		response.setContentType(MediaType.APPLICATION_JSON_TYPE);
		return response;
    }
	
}