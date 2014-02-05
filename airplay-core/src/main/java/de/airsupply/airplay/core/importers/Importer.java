package de.airsupply.airplay.core.importers;

import java.io.InputStream;
import java.util.Date;

import org.springframework.util.Assert;

import de.airsupply.airplay.core.model.Chart;
import de.airsupply.airplay.core.model.PersistentNode;
import de.airsupply.airplay.core.model.RecordImport;
import de.airsupply.commons.core.neo4j.Neo4jServiceSupport;

public abstract class Importer {

	protected <T extends PersistentNode> T importRecord(Neo4jServiceSupport service, RecordImport recordImport, T object) {
		Assert.notNull(service);
		Assert.notNull(object);

		T result = service.find(object);
		if (result == null) {
			T saved = service.save(object);
			if (recordImport != null) {
				recordImport.importRecord(saved);
			}
			return saved;
		}
		return result;
	}

	public abstract void processRecords(RecordImport recordImport, Chart chart, Date week, InputStream inputStream);

}
