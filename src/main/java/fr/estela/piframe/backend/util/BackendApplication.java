package fr.estela.piframe.backend.util;

import java.util.List;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.web.SpringBootServletInitializer;
import org.springframework.boot.orm.jpa.EntityScan;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import fr.estela.piframe.backend.entity.ProviderEntity;
import fr.estela.piframe.backend.repository.ProviderRepository;
import fr.estela.piframe.backend.sourcepack.smugmug.SmugmugProviderEntity;

@SpringBootApplication
@EnableTransactionManagement
@ComponentScan("fr.estela.piframe.backend")
@EnableJpaRepositories("fr.estela.piframe.backend")
@EntityScan("fr.estela.piframe.backend")
@ImportResource({ "classpath:spring-mvc.xml", "classpath:spring-jpa.xml" })
@PropertySource("classpath:local.properties")
public class BackendApplication extends SpringBootServletInitializer {
	
	@Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(BackendApplication.class);
    }

	public static void main(String[] args) throws Exception {
		
		// TODO clean /piframe/tmp folder at startup
		
		ApplicationContext springContext = SpringApplication.run(BackendApplication.class, args);		
	    
	    ProviderRepository providerRepository = springContext.getBean(ProviderRepository.class);
	    List<ProviderEntity> providerEntities = providerRepository.findAll();
	    
	    if (providerEntities.size() == 0) {
		    
			String consumerKey = "QUuIhIFnnuCoMfvxa900joAIQY377u8F";
			String consumerSecret = "a60bda59e1d111fffdf9a4f287334897";
		    String accessToken = "a63b6e0fdf9f6e7fc12583020a4924b0";
		    String accessTokenSecret = "f306746932a1ef7a209211d793353f59e87b3c441c751db041fd266fd8bc3680";
		    
		    SmugmugProviderEntity smugmugProviderEntity = new SmugmugProviderEntity();
		    smugmugProviderEntity.setName("Estela Smugmug account");
		    smugmugProviderEntity.setConsumerKey(consumerKey);
		    smugmugProviderEntity.setConsumerSecret(consumerSecret);
		    smugmugProviderEntity.setAccessToken(accessToken);
		    smugmugProviderEntity.setAccessTokenSecret(accessTokenSecret);
		    smugmugProviderEntity.setAlbum("mgvSgB");
		    
		    providerRepository.save(smugmugProviderEntity);
		    providerEntities.add(smugmugProviderEntity);
	    }
	    
		CamelContext camelContext = springContext.getBean(CamelContext.class);
		camelContext.getShutdownStrategy().setTimeout(10);

		for (final ProviderEntity providerEntity : providerEntities) {
			camelContext.addRoutes(new RouteBuilder() {
				@Override
				public void configure() throws Exception {
					
					String processor = "smugmugDownloadProcessor";
					String providerRouteId = "direct:" + processor + providerEntity.getId();
					
					from(providerRouteId)
						.process("smugmugDownloadProcessor")
						.process("genericProviderPostDownloadProcessor")
						.split(body())
						.process("genericMediaPostDownloadProcessor");				
					
					from("timer://simpleTimer?period=3600000")
						.setProperty("providerEntityId").constant(providerEntity.getId())
						.setProperty("providerRouteId").constant(providerRouteId)
						.dynamicRouter().method("paginatedDownloadRouter");
				}
			});
		}
	
	}
	
}