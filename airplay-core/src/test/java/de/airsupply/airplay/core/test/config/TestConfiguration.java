package de.airsupply.airplay.core.test.config;

import static org.neo4j.helpers.Settings.STRING;
import static org.neo4j.helpers.Settings.setting;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseBuilder;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import de.airsupply.airplay.core.config.DatabaseConfiguration;

@Configuration
@EnableTransactionManagement
@Profile("test")
@PropertySource("classpath:airplay_test.properties")
public class TestConfiguration extends DatabaseConfiguration {

	@Override
	protected void buildGraphDatabaseConfiguration(GraphDatabaseBuilder builder) {
		builder.setConfig(setting("keep_logical_logs", STRING, "0 days"), "0 days");
	}

	@Override
	public GraphDatabaseService graphDatabaseService(String path) {
		return super.graphDatabaseService(System.getProperty("java.io.tmpdir") + "/airplay-store/config-test");
	}

}