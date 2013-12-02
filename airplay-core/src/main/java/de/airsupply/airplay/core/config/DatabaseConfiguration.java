package de.airsupply.airplay.core.config;

import java.util.HashMap;
import java.util.Map;

import org.joda.time.DateTime;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseBuilder;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.data.neo4j.config.EnableNeo4jRepositories;
import org.springframework.data.neo4j.config.Neo4jConfiguration;
import org.springframework.data.neo4j.lifecycle.BeforeSaveEvent;
import org.springframework.data.neo4j.support.typesafety.TypeSafetyOption;
import org.springframework.data.neo4j.support.typesafety.TypeSafetyPolicy;

import de.airsupply.airplay.core.model.PersistentNode;

@Configuration
@Profile("production")
@Import(PropertyConfiguration.class)
@EnableNeo4jRepositories("de.airsupply.airplay.core.graph.repository")
public class DatabaseConfiguration extends Neo4jConfiguration {

	protected Map<String, String> graphDatabaseConfiguration() {
		Map<String, String> configuration = new HashMap<>();
		configuration.put("keep_logical_logs", "7 days");
		return configuration;
	}

	@Bean
	public GraphDatabaseService graphDatabaseService(@Value("${neo4j.path}") String path) {
		GraphDatabaseBuilder builder = new GraphDatabaseFactory().newEmbeddedDatabaseBuilder(path);
		builder.setConfig(graphDatabaseConfiguration());
		return builder.newGraphDatabase();
	}

	@Bean
	public TypeSafetyPolicy typeSafetyPolicy() throws Exception {
		return new TypeSafetyPolicy(TypeSafetyOption.THROWS_EXCEPTION);
	}

	@Bean
	public ApplicationListener<BeforeSaveEvent<PersistentNode>> beforeSaveEventApplicationListener() {
		return new ApplicationListener<BeforeSaveEvent<PersistentNode>>() {

			@Override
			public void onApplicationEvent(BeforeSaveEvent<PersistentNode> event) {
				if (event.getEntity() instanceof PersistentNode) {
					PersistentNode entity = event.getEntity();
					if (entity.isNew()) {
						entity.setCreatedDate(DateTime.now());
					} else {
						entity.setLastModifiedDate(DateTime.now());
					}
				}
			}

		};
	}

}