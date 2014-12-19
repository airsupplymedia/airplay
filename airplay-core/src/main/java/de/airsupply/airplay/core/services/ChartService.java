package de.airsupply.airplay.core.services;

import static de.airsupply.commons.core.util.CollectionUtils.asList;
import static de.airsupply.commons.core.util.DateUtils.getStartOfWeek;
import static java.util.Collections.emptyList;

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

@Service
public class ChartService extends Neo4jServiceSupport {

	public static enum ChartType {

		AIRPLAY("Airplay Charts"), SALES("Sales Charts");

		private String name;

		private ChartType(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}

	}

	@Autowired
	private ChartPositionRepository chartPositionRepository;

	@Autowired
	private ChartRepository chartRepository;

	@Autowired
	private ChartStateRepository chartStateRepository;

	public List<ChartPosition> findChartPositions(Chart chart, Date date) {
		Assert.notNull(chart);
		Assert.notNull(date);
		ChartState chartState = findChartState(chart, date);
		if (chartState == null) {
			return emptyList();
		}
		return chartState.getChartPositionList();
	}

	public List<ChartPosition> findChartPositions(Chart chart, Song song) {
		Assert.notNull(chart);
		Assert.notNull(song);
		return asList(chartPositionRepository.find(chart, song));
	}

	public List<Chart> findCharts(Chart object) {
		Assert.notNull(object);
		return asList(chartRepository.findAllByPropertyValue("name", object.getName()));
	}

	public List<Chart> findCharts(String name) {
		Assert.hasText(name);
		return findCharts(new Chart(name));
	}

	public ChartState findChartState(Chart chart, Date date) {
		Assert.notNull(chart);
		Assert.notNull(date);
		return chartStateRepository.find(chart, getStartOfWeek(date).getTime());
	}

	public List<ChartPosition> findLatestChartPositions(Chart chart) {
		Assert.notNull(chart);
		ChartState chartState = chartStateRepository.findLatest(chart);
		if (chartState == null) {
			return emptyList();
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
		return asList(chartRepository.findAll());
	}

	public long getChartStateCount() {
		return chartStateRepository.count();
	}

	public List<ChartState> getChartStates() {
		return asList(chartStateRepository.findAll());
	}

}
