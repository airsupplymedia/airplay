package de.airsupply.airplay.core.model.test.misc;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import de.airsupply.airplay.core.config.ApplicationConfiguration;
import de.airsupply.airplay.core.model.test.config.BatchConfiguration;
import de.airsupply.commons.core.util.CollectionUtils.Procedure;

public class BatchRunner {

	public static void run(Procedure<ApplicationContext> procedure) {
		AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext();
		applicationContext.getEnvironment().setActiveProfiles("batch");
		applicationContext.register(ApplicationConfiguration.class, BatchConfiguration.class);
		applicationContext.registerShutdownHook();
		applicationContext.refresh();
		applicationContext.start();
		procedure.run(applicationContext);
		applicationContext.stop();
		applicationContext.close();
	}

}
