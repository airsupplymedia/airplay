package de.airsupply.airplay.core.services;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.neo4j.graphdb.Node;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.support.Neo4jTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import de.airsupply.airplay.core.graph.repository.RecordImportRepository;
import de.airsupply.airplay.core.importers.sdf.AirplayRecordImporter;
import de.airsupply.airplay.core.model.Chart;
import de.airsupply.airplay.core.model.ChartState;
import de.airsupply.airplay.core.model.PersistentNode;
import de.airsupply.airplay.core.model.RecordImport;
import de.airsupply.commons.core.neo4j.Neo4jServiceSupport;
import de.airsupply.commons.core.util.CollectionUtils;
import de.airsupply.commons.core.util.CollectionUtils.Filter;
import de.airsupply.commons.core.util.DateUtils;
import de.airsupply.commons.core.util.Functions;

@Service
public class ImportService extends Neo4jServiceSupport {

	@Autowired
	private ChartService chartService;

	@Autowired
	private AirplayRecordImporter importer;

	@Autowired
	private Neo4jTemplate neo4jTemplate;

	@Autowired
	private RecordImportRepository recordImportRepository;

	private RecordImport commitImport(RecordImport recordImport) {
		Assert.notNull(recordImport);
		return save(recordImport);
	}

	public Collection<PersistentNode> getImportedRecordsToRevert(RecordImport recordImport) {
		return getImportedRecordsToRevert(recordImport, null);
	}

	public Collection<PersistentNode> getImportedRecordsToRevert(final RecordImport recordImport, String category) {
		Filter<PersistentNode> filter = new Filter<PersistentNode>() {

			@Override
			public boolean accept(PersistentNode object) {
				return mayRevertImport(object, recordImport);
			}

		};
		if (category == null) {
			return CollectionUtils.filter(recordImport.getImportedRecords(), filter);
		}
		return CollectionUtils.filter(recordImport.getImportedRecords(category), filter);
	}

	public long getRecordImportCount() {
		return recordImportRepository.count();
	}

	public List<RecordImport> getRecordImports() {
		return CollectionUtils.asList(recordImportRepository.findAll());
	}

	@Transactional
	public RecordImport importRecords(Chart chart, Date week, InputStream inputStream) {
		RecordImport recordImport = prepareImport(chart, week, inputStream);
		return commitImport(recordImport);
	}

	private boolean mayRevertImport(PersistentNode persistentNode, RecordImport recordImport) {
		final List<Long> importedRecords = new ArrayList<>();
		importedRecords.add(recordImport.getIdentifier());
		importedRecords.addAll(CollectionUtils.transform(chartService.getCharts(), Functions.toIdentifier()));
		importedRecords.addAll(CollectionUtils.transform(recordImport.getImportedRecords(), Functions.toIdentifier()));

		Set<Node> referencers = chartService.getReferencers(persistentNode);
		Set<Long> referencerIdentifiers = CollectionUtils.transform(referencers, Functions.toId());

		referencerIdentifiers.removeAll(importedRecords);
		return referencerIdentifiers.isEmpty();
	}

	private RecordImport prepareImport(Chart chart, Date week, InputStream inputStream) {
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

	@Transactional
	public void revertImport(RecordImport recordImport) {
		for (PersistentNode persistentNode : getImportedRecordsToRevert(recordImport)) {
			neo4jTemplate.delete(persistentNode);
		}
		recordImportRepository.delete(recordImport);
	}

}
