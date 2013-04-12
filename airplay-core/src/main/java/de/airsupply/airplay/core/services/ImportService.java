package de.airsupply.airplay.core.services;

import java.io.InputStream;
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
import de.airsupply.airplay.core.model.RecordImport;
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
	private RecordImportRepository recordImportRepository;

	private void commitImport(final RecordImport recordImport) {
		Assert.notNull(recordImport);
		findOrCreate(recordImport, true);
	}

	public long getRecordImportCount() {
		return recordImportRepository.count();
	}

	public List<RecordImport> getRecordImports() {
		return CollectionUtils.asList(recordImportRepository.findAll());
	}

	@Transactional
	public RecordImport importRecords(final Chart chart, Date week, final InputStream inputStream) {
		RecordImport recordImport = prepareImport(chart, week, inputStream);
		commitImport(recordImport);
		return recordImport;
	}

	private RecordImport prepareImport(final Chart chart, Date week, final InputStream inputStream) {
		Assert.notNull(chart);
		Assert.notNull(week);
		Assert.notNull(inputStream);

		week = DateUtils.getStartOfWeek(week);

		RecordImport recordImport = new RecordImport(week);

		Assert.isTrue(!exists(recordImport), "Import for week " + DateUtils.getWeekOfYearFormat(week)
				+ " has been performed before!");

		ChartState chartState = chartService.save(new ChartState(chart, week));
		importer.processRecords(inputStream, chartState, recordImport);

		return recordImport;
	}

}
