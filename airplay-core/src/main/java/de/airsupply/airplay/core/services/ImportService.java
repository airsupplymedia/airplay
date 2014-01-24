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
import de.airsupply.airplay.core.importers.sdf.AirplayRecordImporter;
import de.airsupply.airplay.core.model.Chart;
import de.airsupply.airplay.core.model.ChartState;
import de.airsupply.airplay.core.model.PersistentNode;
import de.airsupply.airplay.core.model.RecordImport;
import de.airsupply.airplay.core.model.util.LoggingRecordImportProgressProvider;
import de.airsupply.airplay.core.model.util.RecordImportProgressProvider;
import de.airsupply.commons.core.neo4j.Neo4jServiceSupport;
import de.airsupply.commons.core.util.CollectionUtils;
import de.airsupply.commons.core.util.DateUtils;

@Service
public class ImportService extends Neo4jServiceSupport {

	@Autowired
	private ChartService chartService;

	@Autowired
	private AirplayRecordImporter importer;

	@Autowired
	private LoggingRecordImportProgressProvider loggingRecordImportProgressProvider;

	@Autowired
	private RecordImportRepository recordImportRepository;

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
	public RecordImport importRecords(Chart chart, Date week, InputStream inputStream) {
		return importRecords(chart, week, inputStream, loggingRecordImportProgressProvider);
	}

	@Transactional
	public RecordImport importRecords(Chart chart, Date week, InputStream inputStream,
			RecordImportProgressProvider progressProvider) {
		RecordImport recordImport = commitImport(prepareImport(chart, week, inputStream, progressProvider));
		progressProvider.imported(recordImport);
		return recordImport;
	}

	private RecordImport prepareImport(Chart chart, Date week, InputStream inputStream,
			RecordImportProgressProvider progressProvider) {
		Assert.notNull(chart);
		Assert.notNull(week);
		Assert.notNull(inputStream);

		week = DateUtils.getStartOfWeek(week);
		RecordImport recordImport = new RecordImport(week);

		Assert.isTrue(!exists(recordImport), "Import for week " + DateUtils.getWeekOfYearFormat(week)
				+ " has been performed before!");

		ChartState chartState = chartService.save(new ChartState(chart, week));
		importer.processRecords(inputStream, chartState, recordImport, progressProvider);
		return recordImport;
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
