package de.airsupply.airplay.core.test.misc;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import de.airsupply.airplay.core.config.ApplicationConfiguration;
import de.airsupply.airplay.core.test.config.BatchConfiguration;
import de.airsupply.airplay.core.test.config.TestConfiguration;
import de.airsupply.commons.core.util.CollectionUtils.Procedure;

public class BatchRunner {

	public static void run(Procedure<ApplicationContext> procedure) {
		run(procedure, "batch");
	}

	public static void run(Procedure<ApplicationContext> procedure, String activeProfile) {
		AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext();
		applicationContext.getEnvironment().setActiveProfiles(activeProfile);
		applicationContext.register(ApplicationConfiguration.class, TestConfiguration.class, BatchConfiguration.class);
		applicationContext.registerShutdownHook();
		applicationContext.refresh();
		applicationContext.start();
		procedure.run(applicationContext);
		applicationContext.stop();
		applicationContext.close();
	}

}
