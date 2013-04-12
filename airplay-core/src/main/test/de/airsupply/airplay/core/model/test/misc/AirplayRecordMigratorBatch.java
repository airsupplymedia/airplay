package de.airsupply.airplay.core.model.test.misc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;

import de.airsupply.airplay.core.importers.dbf.AirplayRecordMigrator;
import de.airsupply.airplay.core.services.ChartService;

@Component
public class AirplayRecordMigratorBatch {

	public static void main(String[] args) {
		ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext(
				"classpath*:META-INF/spring/applicationContext*.xml");
		applicationContext.start();
		applicationContext.getBean(AirplayRecordMigratorBatch.class).prefill();
		applicationContext.stop();
		applicationContext.close();
	}

	@Autowired
	private ChartService chartService;

	@Autowired
	private AirplayRecordMigrator migrator;

	private void prefill() {
		chartService.createInitialData();
		migrator.migrate();
	}

}
