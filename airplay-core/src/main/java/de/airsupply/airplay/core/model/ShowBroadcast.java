package de.airsupply.airplay.core.model;

import java.util.Date;

import javax.validation.constraints.NotNull;

import org.springframework.data.neo4j.annotation.RelationshipEntity;
import org.springframework.data.neo4j.annotation.StartNode;

import de.airsupply.commons.core.neo4j.annotation.Persistent;
import de.airsupply.commons.core.neo4j.annotation.Unique;

@Unique(query = "START show=node({broadcastedShow}), station=node({station}) MATCH show-[showBroadcast:SHOW_BROADCAST]->station WHERE showBroadcast.from={from} AND showBroadcast.to={to} RETURN showBroadcast", arguments = {
		"broadcastedShow", "station", "from", "to" })
@RelationshipEntity(type = "SHOW_BROADCAST")
@SuppressWarnings("serial")
public class ShowBroadcast extends Broadcast {

	@NotNull
	@Persistent
	@StartNode
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
