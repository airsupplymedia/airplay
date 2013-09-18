package de.airsupply.airplay.core.test.config;

import java.util.HashMap;
import java.util.Map;

import org.neo4j.graphdb.GraphDatabaseService;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import de.airsupply.airplay.core.config.DatabaseConfiguration;

@Configuration
@Profile("test")
public class TestConfiguration extends DatabaseConfiguration {

	@Override
	protected Map<String, String> graphDatabaseConfiguration() {
		Map<String, String> configuration = new HashMap<>();
		configuration.put("keep_logical_logs", "0 days");
		return configuration;
	}

	@Override
	public GraphDatabaseService graphDatabaseService(String path) {
		return super.graphDatabaseService(System.getProperty("java.io.tmpdir") + "/airplay-store/config-test");
	}

}