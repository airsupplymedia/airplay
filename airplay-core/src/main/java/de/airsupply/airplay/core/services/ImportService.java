package de.airsupply.airplay.core.services;

import java.io.InputStream;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import de.airsupply.airplay.core.graph.repository.RecordImportRepository;
import de.airsupply.airplay.core.importers.Importer;
import de.airsupply.airplay.core.importers.sdf.SDFImporter;
import de.airsupply.airplay.core.importers.xls.XLSImporter;
import de.airsupply.airplay.core.model.Chart;
import de.airsupply.airplay.core.model.PersistentNode;
import de.airsupply.airplay.core.model.RecordImport;
import de.airsupply.commons.core.neo4j.Neo4jServiceSupport;
import de.airsupply.commons.core.util.CollectionUtils;
import de.airsupply.commons.core.util.DateUtils;

@Service
public class ImportService extends Neo4jServiceSupport {

	public static enum ImporterType {

		SDF("sdf"), XLS("xls");

		private String identifier;

		private ImporterType(String identifier) {
			this.identifier = identifier;
		}

		public static ImporterType getByFileName(String fileName) {
			Assert.notNull(fileName);
			for (ImporterType importerType : values()) {
				if (fileName.toLowerCase().endsWith(importerType.getIdentifier().toLowerCase())) {
					return importerType;
				}
			}
			return null;
		}

		public String getIdentifier() {
			return identifier;
		}

	}

	@Autowired
	private ChartService chartService;

	@Autowired
	private RecordImportRepository recordImportRepository;

	@Autowired
	private SDFImporter sdfImporter;

	@Autowired
	private XLSImporter xlsImporter;

	private RecordImport commitImport(RecordImport recordImport) {
		Assert.notNull(recordImport);
		return save(recordImport);
	}

	public Collection<PersistentNode> getImportedRecordsToRevert(RecordImport recordImport) {
		Assert.notNull(recordImport);
		return recordImport.getImportedRecordsWithoutDependees(getNeo4jTemplate());
	}

	public long getRecordImportCount() {
		return recordImportRepository.count();
	}

	public List<RecordImport> getRecordImports() {
		return CollectionUtils.asList(recordImportRepository.findAll());
	}

	@Transactional
	public RecordImport importRecords(ImporterType importerType, Chart chart, Date week, InputStream inputStream) {
		return commitImport(prepareImport(importerType, chart, week, inputStream));
	}

	private RecordImport prepareImport(ImporterType importerType, Chart chart, Date week, InputStream inputStream) {
		Assert.notNull(importerType);
		Assert.notNull(chart);
		Assert.notNull(week);
		Assert.notNull(inputStream);

		week = DateUtils.getStartOfWeek(week);
		RecordImport recordImport = new RecordImport(week);

		Assert.isTrue(!exists(recordImport), "Import for week " + DateUtils.getWeekOfYearFormat(week)
				+ " has been performed before!");

		getImporter(importerType).processRecords(recordImport, chart, week, inputStream);

		return recordImport;
	}

	private Importer getImporter(ImporterType importerType) {
		switch (importerType) {
		case SDF:
			return sdfImporter;
		case XLS:
			return xlsImporter;
		default:
			return null;
		}
	}

	@Transactional
	public void revertImport(RecordImport recordImport) {
		Assert.notNull(recordImport);
		for (PersistentNode importedRecord : getImportedRecordsToRevert(recordImport)) {
			getNeo4jTemplate().delete(importedRecord);
		}
		recordImportRepository.delete(recordImport);
	}

}
