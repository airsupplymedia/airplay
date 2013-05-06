package de.airsupply.airplay.core.model;

import javax.validation.constraints.NotNull;

import org.springframework.data.neo4j.annotation.EndNode;
import org.springframework.data.neo4j.annotation.RelationshipEntity;
import org.springframework.data.neo4j.annotation.StartNode;

import de.airsupply.commons.core.neo4j.annotation.Unique;

@Unique(query = "START chartState=node({chartState}) MATCH chartState-[chartPosition:CHART_POSITIONS]->() WHERE chartPosition.position={position} RETURN chartPosition", arguments = {
		"chartState", "position" })
@RelationshipEntity(type = "CHART_POSITIONS")
@SuppressWarnings("serial")
public class ChartPosition extends PersistentNode {

	@NotNull
	@StartNode
	private ChartState chartState;

	private int position;

	@NotNull
	@EndNode
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
