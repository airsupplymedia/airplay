package de.airsupply.airplay.core.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import de.airsupply.commons.core.context.LoggerBeanFactoryPostProcessor;

@Configuration
@PropertySource("classpath:airplay.properties")
public class PropertyConfiguration {

	@Bean
	public static LoggerBeanFactoryPostProcessor loggerBeanFactoryPostProcessor() {
		return new LoggerBeanFactoryPostProcessor();
	}

	@Bean
	public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
		PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer = new PropertySourcesPlaceholderConfigurer();
		propertySourcesPlaceholderConfigurer.setLocation(new ClassPathResource("airplay.properties"));
		return propertySourcesPlaceholderConfigurer;
	}

	@Bean
	public LocalValidatorFactoryBean validator() {
		return new LocalValidatorFactoryBean();
	}

}