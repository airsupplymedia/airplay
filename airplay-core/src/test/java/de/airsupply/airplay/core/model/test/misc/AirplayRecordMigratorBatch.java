package de.airsupply.airplay.core.model.test.misc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;

import de.airsupply.airplay.core.importers.dbf.AirplayRecordMigrator;

@Component
public class AirplayRecordMigratorBatch {

	public static void main(String[] args) {
		AbstractApplicationContext applicationContext = new ClassPathXmlApplicationContext(
				"classpath*:/applicationContext-batch.xml");
		applicationContext.registerShutdownHook();
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
