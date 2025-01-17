package de.airsupply.airplay.core.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
@Import(DatabaseConfiguration.class)
@ComponentScan({ "de.airsupply.airplay.core", "de.airsupply.commons.core" })
@PropertySource("classpath:airplay.properties")
public class ApplicationConfiguration {
}