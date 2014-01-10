package de.airsupply.airplay.core.model;

import javax.validation.constraints.NotNull;

import org.neo4j.graphdb.Direction;
import org.springframework.data.neo4j.annotation.Fetch;
import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.data.neo4j.annotation.RelatedTo;

import com.fasterxml.jackson.annotation.JsonIgnore;

import de.airsupply.commons.core.neo4j.annotation.Unique;

@Unique(query = "START chartState=node({chartState}), song=node({song}) MATCH chartState-[:CHART_POSITIONS]-chartPosition-[:CHART_POSITION]->song WHERE chartPosition.position={position} RETURN chartPosition", arguments = {
		"chartState", "position", "song" })
@NodeEntity
@SuppressWarnings("serial")
public class ChartPosition extends PersistentNode {

	@NotNull
	@RelatedTo(direction = Direction.INCOMING, type = "CHART_POSITIONS")
	@JsonIgnore
	private ChartState chartState;

	private int position;

	@Fetch
	@NotNull
	@RelatedTo(direction = Direction.OUTGOING, type = "CHART_POSITION")
	private Song song;

	ChartPosition() {
		super();
	}

	public ChartPosition(ChartState chartState, Song song, int position) {
		super();
		this.chartState = chartState;
		this.song = song;
		this.position = position;
	}

	@JsonIgnore
	public ChartState getChartState() {
		return chartState;
	}

	public int getPosition() {
		return position;
	}

	public Song getSong() {
		return song;
	}

	@Override
	public String toString() {
		return "ChartPosition [chartState=" + chartState + ", position=" + position + ", song=" + song + "]";
	}

}
