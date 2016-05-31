package io.swagger.inflector.config;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Hack of Default ControllerFactory implementation
 * until the controllerFactory config option is available
 */
public class DefaultControllerFactory implements ControllerFactory {

	private ApplicationContext applicationContext;
	
	public DefaultControllerFactory() {
		applicationContext = new ClassPathXmlApplicationContext("spring-config.xml");
	}

	/**
	 * Instantiates the provided class using the Spring Container
	 *
	 * @param cls the class to be instantiated
	 * @return an instance of the provided class
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	@Override
	public Object instantiateController(Class<? extends Object> cls) throws IllegalAccessException, InstantiationException {
		return applicationContext.getBean(cls);
	}
}