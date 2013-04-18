package de.airsupply.airplay.core.model;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.neo4j.graphdb.Direction;
import org.springframework.data.neo4j.annotation.Indexed;
import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.data.neo4j.annotation.RelatedTo;
import org.springframework.data.neo4j.annotation.RelatedToVia;

import de.airsupply.commons.core.neo4j.annotation.Unique;
import de.airsupply.commons.core.util.CollectionUtils;

@Unique(query = "START chart=node({chart}) MATCH chart-[:CHART_STATES]->chartState WHERE chartState.week={week} RETURN chartState", arguments = {
		"chart", "week" })
@NodeEntity
@SuppressWarnings("serial")
public class ChartState extends PersistentNode {

	@NotNull
	@RelatedTo(direction = Direction.INCOMING, type = "CHART_STATES")
	private Chart chart;

	@RelatedToVia(direction = Direction.OUTGOING, type = "CHART_POSITIONS")
	private Iterable<ChartPosition> chartPositions = null;

	@Indexed
	private long week;

	ChartState() {
		super();
	}

	public ChartState(Chart chart, Date week) {
		super();
		this.chart = chart;
		this.week = week.getTime();
	}

	public Chart getChart() {
		return chart;
	}

	public List<ChartPosition> getChartPositionList() {
		if (chartPositions != null) {
			return CollectionUtils.asList(chartPositions);
		} else {
			return Collections.emptyList();
		}
	}

	public Date getWeekDate() {
		return new Date(week);
	}

	@Override
	public String toString() {
		return "ChartState [chart=" + chart + ", week=" + week + ", getIdentifier()=" + getIdentifier() + "]";
	}

	private void writeObject(ObjectOutputStream outputStream) throws IOException {
		chartPositions = null;
		outputStream.defaultWriteObject();
	}

}
