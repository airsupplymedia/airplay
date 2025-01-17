package de.airsupply.airplay.core.test.misc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import de.airsupply.airplay.core.importers.dbf.DBFImporter;
import de.airsupply.commons.core.util.CollectionUtils.Procedure;

@Component
public class DBFImporterBatch {

	public static void main(String[] args) {
		BatchRunner.run(new Procedure<ApplicationContext>() {

			@Override
			public void run(ApplicationContext applicationContext) {
				applicationContext.getBean(DBFImporterBatch.class).prefill();
			}

		});
	}

	@Autowired
	private DBFImporter migrator;

	private void prefill() {
		migrator.migrate("C:\\Development\\Storage\\Git\\airplay\\airplay-dbf",
				"C:/Development/Storage/Neo4j/config-batch-small");
	}
}
