package de.airsupply.airplay.core.services;

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

	public List<ChartPosition> findChartPositions(Chart chart, Date date) {
		Assert.notNull(chart);
		Assert.notNull(date);
		return CollectionUtils.asList(chartPositionRepository.find(chart, DateUtils.getStartOfWeek(date).getTime()));
	}

	public List<ChartPosition> findChartPositions(Chart chart, Song song) {
		Assert.notNull(chart);
		Assert.notNull(song);
		return CollectionUtils.asList(chartPositionRepository.find(chart, song));
	}

	public long getChartCount() {
		return chartRepository.count();
	}

	public long getChartPositionCount() {
		return chartPositionRepository.count();
	}

	public List<Chart> getCharts() {
		return CollectionUtils.asList(chartRepository.findAll());
	}

	public long getChartStateCount() {
		return chartStateRepository.count();
	}

	public List<ChartState> getChartStates() {
		return CollectionUtils.asList(chartStateRepository.findAll());
	}

}
