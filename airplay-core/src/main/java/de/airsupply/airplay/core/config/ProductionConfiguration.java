package de.airsupply.airplay.core.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import de.airsupply.commons.core.context.LoggerBeanFactoryPostProcessor;

@Configuration
public class ProductionConfiguration {

	@Bean
	public LoggerBeanFactoryPostProcessor loggerBeanFactoryPostProcessor() {
		return new LoggerBeanFactoryPostProcessor();
	}

	@Bean
	public LocalValidatorFactoryBean validator() {
		return new LocalValidatorFactoryBean();
	}

}
