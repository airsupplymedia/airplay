package de.airsupply.airplay.core.config;

import static org.neo4j.helpers.Settings.STRING;
import static org.neo4j.helpers.Settings.setting;

import org.joda.time.DateTime;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseBuilder;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.neo4j.config.EnableNeo4jRepositories;
import org.springframework.data.neo4j.config.Neo4jConfiguration;
import org.springframework.data.neo4j.lifecycle.BeforeSaveEvent;
import org.springframework.data.neo4j.support.typesafety.TypeSafetyOption;
import org.springframework.data.neo4j.support.typesafety.TypeSafetyPolicy;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import de.airsupply.airplay.core.model.PersistentNode;
import de.airsupply.commons.core.context.LoggerBeanFactoryPostProcessor;

@Configuration
@Profile("production")
@EnableNeo4jRepositories("de.airsupply.airplay.core.graph.repository")
public class DatabaseConfiguration extends Neo4jConfiguration {

	public DatabaseConfiguration() {
		setBasePackage("de.airsupply.airplay.core.model");
	}

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

	protected void buildGraphDatabaseConfiguration(GraphDatabaseBuilder builder) {
		builder.setConfig(setting("keep_logical_logs", STRING, "7 days"), "7 days");
	}

	@Bean
	public GraphDatabaseService graphDatabaseService(@Value("${neo4j.path}") String path) {
		GraphDatabaseBuilder builder = new GraphDatabaseFactory().newEmbeddedDatabaseBuilder(path);
		buildGraphDatabaseConfiguration(builder);
		return builder.newGraphDatabase();
	}

	@Bean
	public TypeSafetyPolicy typeSafetyPolicy() throws Exception {
		return new TypeSafetyPolicy(TypeSafetyOption.THROWS_EXCEPTION);
	}

	@Bean
	public LocalValidatorFactoryBean validator() {
		return new LocalValidatorFactoryBean();
	}

}