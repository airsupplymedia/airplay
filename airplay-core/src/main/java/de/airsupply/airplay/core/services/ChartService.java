package de.airsupply.airplay.core.services;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import de.airsupply.airplay.core.graph.repository.ChartPositionRepository;
import de.airsupply.airplay.core.graph.repository.ChartRepository;
import de.airsupply.airplay.core.graph.repository.ChartStateRepository;
import de.airsupply.airplay.core.model.Chart;
import de.airsupply.airplay.core.model.ChartPosition;
import de.airsupply.airplay.core.model.ChartState;
import de.airsupply.airplay.core.model.Song;
import de.airsupply.commons.core.neo4j.Neo4jServiceSupport;
import de.airsupply.commons.core.util.CollectionUtils;
import de.airsupply.commons.core.util.DateUtils;

@Service
public class ChartService extends Neo4jServiceSupport {

	@Autowired
	private ChartPositionRepository chartPositionRepository;

	@Autowired
	private ChartRepository chartRepository;

	@Autowired
	private ChartStateRepository chartStateRepository;

	private void createDefaultCharts() {
		save(new Chart("Airplay Charts"));
		save(new Chart("Sales Charts"));
	}

	public List<ChartPosition> findChartPositions(Chart chart, Date date) {
		Assert.notNull(chart);
		Assert.notNull(date);
		ChartState chartState = chartStateRepository.find(chart, DateUtils.getStartOfWeek(date).getTime());
		if (chartState == null) {
			return Collections.emptyList();
		}
		return chartState.getChartPositionList();
	}

	public List<ChartPosition> findChartPositions(Chart chart, Song song) {
		Assert.notNull(chart);
		Assert.notNull(song);
		return CollectionUtils.asList(chartPositionRepository.find(chart, song));
	}

	public List<ChartPosition> findLatestChartPositions(Chart chart) {
		Assert.notNull(chart);
		ChartState chartState = chartStateRepository.findLatest(chart);
		if (chartState == null) {
			return Collections.emptyList();
		}
		return chartState.getChartPositionList();
	}

	public long getChartCount() {
		return chartRepository.count();
	}

	public long getChartPositionCount() {
		return chartPositionRepository.count();
	}

	public List<Chart> getCharts() {
		List<Chart> charts = CollectionUtils.asList(chartRepository.findAll());
		if (charts.isEmpty()) {
			createDefaultCharts();
		}
		return charts;
	}

	public long getChartStateCount() {
		return chartStateRepository.count();
	}

	public List<ChartState> getChartStates() {
		return CollectionUtils.asList(chartStateRepository.findAll());
	}

}
