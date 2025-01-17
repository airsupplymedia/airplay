package de.airsupply.airplay.core.model;

import static de.airsupply.commons.core.util.CollectionUtils.asModifiableList;
import static java.util.Collections.emptyList;
import static java.util.Collections.sort;
import static java.util.Collections.unmodifiableList;

import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.neo4j.graphdb.Direction;
import org.springframework.data.neo4j.annotation.Fetch;
import org.springframework.data.neo4j.annotation.Indexed;
import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.data.neo4j.annotation.RelatedTo;

import com.fasterxml.jackson.annotation.JsonIgnore;

import de.airsupply.commons.core.neo4j.annotation.Persistent;
import de.airsupply.commons.core.neo4j.annotation.Unique;

@Unique(query = "START chart=node({chart}) MATCH chart<-[:CHART_STATES]->chartState WHERE chartState.week={week} RETURN chartState", parameters = {
		"chart", "week" })
@NodeEntity
@SuppressWarnings("serial")
public class ChartState extends PersistentNode {

	@Persistent
	@NotNull
	@RelatedTo(direction = Direction.BOTH, type = "CHART_STATES")
	@JsonIgnore
	private Chart chart;

	@Fetch
	@RelatedTo(direction = Direction.BOTH, type = "CHART_POSITIONS")
	@JsonIgnore
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

	@JsonIgnore
	public List<ChartPosition> getChartPositionList() {
		if (chartPositions != null) {
			List<ChartPosition> list = asModifiableList(chartPositions);
			sort(list, new Comparator<ChartPosition>() {

				@Override
				public int compare(ChartPosition o1, ChartPosition o2) {
					return Integer.valueOf(o1.getPosition()).compareTo(Integer.valueOf(o2.getPosition()));
				}

			});
			return unmodifiableList(list);
		} else {
			return emptyList();
		}
	}

	public Date getWeekDate() {
		return new Date(week);
	}

	@Override
	public String toString() {
		return "ChartState [chart=" + chart + ", week=" + week + ", getIdentifier()=" + getIdentifier() + "]";
	}

}
