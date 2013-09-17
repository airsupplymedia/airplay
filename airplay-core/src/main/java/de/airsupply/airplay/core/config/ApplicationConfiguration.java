package de.airsupply.airplay.core.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(DatabaseConfiguration.class)
@ComponentScan({ "de.airsupply.airplay.core", "de.airsupply.commons.core" })
public class ApplicationConfiguration {
}
