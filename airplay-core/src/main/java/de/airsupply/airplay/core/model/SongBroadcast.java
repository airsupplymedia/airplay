package de.airsupply.airplay.core.model;

import java.util.Date;

import javax.validation.constraints.NotNull;

import org.springframework.data.neo4j.annotation.RelatedTo;

import de.airsupply.airplay.core.model.Traversers.SongBroadcastUniquenessTraverserFactory;
import de.airsupply.commons.core.neo4j.annotation.Persistent;
import de.airsupply.commons.core.neo4j.annotation.Unique;

@Unique(traverser = SongBroadcastUniquenessTraverserFactory.class, parameters = { "broadcastedSong", "station", "from",
		"to" })
@SuppressWarnings("serial")
public class SongBroadcast extends Broadcast {

	@NotNull
	@Persistent
	@RelatedTo(type = "SONG_BROADCAST_OF")
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
