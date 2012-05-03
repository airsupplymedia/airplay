package de.airsupply.airplay.core.model;

import java.util.Date;

import javax.validation.constraints.NotNull;

import org.springframework.data.neo4j.annotation.RelationshipEntity;
import org.springframework.data.neo4j.annotation.StartNode;

import de.airsupply.commons.core.neo4j.annotation.Persistent;
import de.airsupply.commons.core.neo4j.annotation.Unique;

@Unique(query = "START song=node({broadcastedSong}), station=node({station}) MATCH song-[songBroadcast:SONG_BROADCAST]->station WHERE songBroadcast.from={from} AND songBroadcast.to={to} RETURN songBroadcast", arguments = {
		"broadcastedSong", "station", "from", "to" })
@RelationshipEntity(type = "SONG_BROADCAST")
@SuppressWarnings("serial")
public class SongBroadcast extends Broadcast {

	@NotNull
	@Persistent
	@StartNode
	private Song broadcastedSong;

	SongBroadcast() {
		super();
	}

	public SongBroadcast(Station station, Song broadcastedSong, Date time) {
		super(station, BroadcastType.EXACT_BEGIN, time, 1);
		this.broadcastedSong = broadcastedSong;
	}

	public SongBroadcast(Station station, Song broadcastedSong, Date week, int count) {
		super(station, BroadcastType.WEEK, week, count);
		this.broadcastedSong = broadcastedSong;
	}

	public Song getBroadcastedSong() {
		return broadcastedSong;
	}

	@Override
	public String toString() {
		return "SongBroadcast [broadcastedSong=" + broadcastedSong + ", getBroadcastType()=" + getBroadcastType()
				+ ", getCount()=" + getCount() + ", getFromDate()=" + getFromDate() + ", getStation()=" + getStation()
				+ ", getToDate()=" + getToDate() + ", getIdentifier()=" + getIdentifier() + "]";
	}

}
