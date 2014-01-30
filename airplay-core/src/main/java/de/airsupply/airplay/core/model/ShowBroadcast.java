package de.airsupply.airplay.core.model;

import java.util.Date;

import javax.validation.constraints.NotNull;

import org.neo4j.graphdb.Direction;
import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.data.neo4j.annotation.RelatedTo;

import de.airsupply.airplay.core.model.Traversers.ShowBroadcastUniquenessTraverserFactory;
import de.airsupply.commons.core.neo4j.annotation.Persistent;
import de.airsupply.commons.core.neo4j.annotation.Unique;

@Unique(traverser = ShowBroadcastUniquenessTraverserFactory.class, parameters = { "broadcastedShow", "station", "from",
		"to" })
@NodeEntity
@SuppressWarnings("serial")
public class ShowBroadcast extends Broadcast {

	@NotNull
	@Persistent
	@RelatedTo(direction = Direction.OUTGOING, type = "SHOW_BROADCAST_OF")
	private Show broadcastedShow;

	ShowBroadcast() {
		super();
	}

	public ShowBroadcast(Station station, Show broadcastedShow, Date time) {
		super(station, BroadcastType.EXACT_BEGIN, time, 1);
		this.broadcastedShow = broadcastedShow;
	}

	public Show getBroadcastedShow() {
		return broadcastedShow;
	}

	@Override
	public String toString() {
		return "ShowBroadcast [broadcastedShow=" + broadcastedShow + ", getBroadcastType()=" + getBroadcastType()
				+ ", getCount()=" + getCount() + ", getFromDate()=" + getFromDate() + ", getStation()=" + getStation()
				+ ", getToDate()=" + getToDate() + ", getIdentifier()=" + getIdentifier() + "]";
	}

}
