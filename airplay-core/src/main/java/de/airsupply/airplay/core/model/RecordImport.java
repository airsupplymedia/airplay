package de.airsupply.airplay.core.model;

import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.neo4j.graphdb.Direction;
import org.springframework.data.neo4j.annotation.Indexed;
import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.data.neo4j.annotation.RelatedTo;
import org.springframework.util.Assert;

import de.airsupply.commons.core.neo4j.annotation.Unique;
import de.airsupply.commons.core.util.CollectionUtils;

@Unique(arguments = { "week" })
@NodeEntity
@SuppressWarnings("serial")
public class RecordImport extends PersistentNode {

	@RelatedTo(direction = Direction.OUTGOING, type = "IMPORTED_ARTISTS")
	private Set<Artist> importedArtists = new HashSet<>();

	@RelatedTo(direction = Direction.OUTGOING, type = "IMPORTED_CHART_STATES")
	private Set<ChartState> importedChartStates = new HashSet<>();

	@RelatedTo(direction = Direction.OUTGOING, type = "IMPORTED_PUBLISHERS")
	private Set<Publisher> importedPublishers = new HashSet<>();

	@RelatedTo(direction = Direction.OUTGOING, type = "IMPORTED_RECORD_COMPANIES")
	private Set<RecordCompany> importedRecordCompanies = new HashSet<>();

	@RelatedTo(direction = Direction.OUTGOING, type = "IMPORTED_SHOW_BROADCASTS")
	private Set<ShowBroadcast> importedShowBroadcasts = new HashSet<>();

	@RelatedTo(direction = Direction.OUTGOING, type = "IMPORTED_SONG_BROADCASTS")
	private Set<SongBroadcast> importedSongBroadcasts = new HashSet<>();

	@RelatedTo(direction = Direction.OUTGOING, type = "IMPORTED_SONGS")
	private Set<Song> importedSongs = new HashSet<>();

	@RelatedTo(direction = Direction.OUTGOING, type = "IMPORTED_STATIONS")
	private Set<Station> importedStations = new HashSet<>();

	@Indexed
	private long week;

	RecordImport() {
		super();
	}

	public RecordImport(Date week) {
		super();
		Assert.notNull(week);
		this.week = week.getTime();
	}

	public Set<Artist> getImportedArtistList() {
		return importedArtists;
	}

	public List<ChartState> getImportedChartStateList() {
		if (importedChartStates != null) {
			return CollectionUtils.asList(importedChartStates);
		} else {
			return Collections.emptyList();
		}
	}

	public Set<Publisher> getImportedPublisherList() {
		return importedPublishers;
	}

	public Set<RecordCompany> getImportedRecordCompanyList() {
		return importedRecordCompanies;
	}

	public List<ShowBroadcast> getImportedShowBroadcastList() {
		if (importedShowBroadcasts != null) {
			return CollectionUtils.asList(importedShowBroadcasts);
		} else {
			return Collections.emptyList();
		}
	}

	public List<SongBroadcast> getImportedSongBroadcastList() {
		if (importedSongBroadcasts != null) {
			return CollectionUtils.asList(importedSongBroadcasts);
		} else {
			return Collections.emptyList();
		}
	}

	public Set<Song> getImportedSongList() {
		return importedSongs;
	}

	public Set<Station> getImportedStationList() {
		return importedStations;
	}

	public Date getWeekDate() {
		return new Date(week);
	}

	public void importArtist(Artist artist) {
		importedArtists.add(artist);
	}

	public void importBroadcast(ShowBroadcast broadcast) {
		importedShowBroadcasts.add(broadcast);
	}

	public void importBroadcast(SongBroadcast broadcast) {
		importedSongBroadcasts.add(broadcast);
	}

	public void importChartState(ChartState chartState) {
		importedChartStates.add(chartState);
	}

	public void importPublisher(Publisher publisher) {
		importedPublishers.add(publisher);
	}

	public void importRecordCompany(RecordCompany recordCompany) {
		importedRecordCompanies.add(recordCompany);
	}

	public void importSong(Song song) {
		importedSongs.add(song);
	}

	public void importStation(Station station) {
		importedStations.add(station);
	}

}
