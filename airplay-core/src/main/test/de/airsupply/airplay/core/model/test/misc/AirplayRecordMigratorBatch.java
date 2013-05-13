package de.airsupply.airplay.core.model.test.misc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;

import de.airsupply.airplay.core.importers.dbf.AirplayRecordMigrator;

@Component
public class AirplayRecordMigratorBatch {

	public static void main(String[] args) {
		ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext(
				"classpath*:de/airsupply/airplay/core/model/test/misc/applicationContext-batch.xml");
		applicationContext.start();
		applicationContext.getBean(AirplayRecordMigratorBatch.class).prefill();
		applicationContext.stop();
		applicationContext.close();
	}

	@Autowired
	private AirplayRecordMigrator migrator;

	private void prefill() {
		migrator.migrate("C:\\Development\\Storage\\Git\\airplay\\airplay-dbf",
				"C:/Development/Storage/Neo4j/config-batch");
	}
}
