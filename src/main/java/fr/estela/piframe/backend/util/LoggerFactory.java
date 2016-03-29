package fr.estela.piframe.backend.util;

import org.slf4j.Logger;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.stereotype.Component;

@Component
public class LoggerFactory implements FactoryBean<Logger> {

	@Override
	public Logger getObject() throws Exception {
		return org.slf4j.LoggerFactory.getLogger("Backend");
	}

	@Override
	public Class<Logger> getObjectType() {
		return Logger.class;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}	
}