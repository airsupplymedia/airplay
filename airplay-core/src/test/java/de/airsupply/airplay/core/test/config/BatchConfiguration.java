package de.airsupply.airplay.core.test.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import de.airsupply.airplay.core.config.DatabaseConfiguration;

@Configuration
@Profile("batch")
public class BatchConfiguration extends DatabaseConfiguration {
}