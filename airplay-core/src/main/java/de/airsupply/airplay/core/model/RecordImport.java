package de.airsupply.airplay.core.model;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.neo4j.graphdb.Direction;
import org.springframework.data.neo4j.annotation.Indexed;
import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.data.neo4j.annotation.RelatedTo;
import org.springframework.util.Assert;

import de.airsupply.commons.core.neo4j.annotation.Persistent;
import de.airsupply.commons.core.neo4j.annotation.Unique;
import de.airsupply.commons.core.util.CollectionUtils;

@Unique(arguments = { "week" })
@NodeEntity
@SuppressWarnings("serial")
public class RecordImport extends PersistentNode {

	@Persistent
	@RelatedTo(direction = Direction.OUTGOING, type = "IMPORTED_ARTISTS")
	private Set<Artist> importedArtists = new HashSet<>();

	@Persistent
	@RelatedTo(direction = Direction.OUTGOING, type = "IMPORTED_CHART_POSITIONS")
	private Set<ChartPosition> importedChartPositions = new HashSet<>();

	@Persistent
	@RelatedTo(direction = Direction.OUTGOING, type = "IMPORTED_CHART_STATES")
	private Set<ChartState> importedChartStates = new HashSet<>();

	@Persistent
	@RelatedTo(direction = Direction.OUTGOING, type = "IMPORTED_PUBLISHERS")
	private Set<Publisher> importedPublishers = new HashSet<>();

	@Persistent
	@RelatedTo(direction = Direction.OUTGOING, type = "IMPORTED_RECORD_COMPANIES")
	private Set<RecordCompany> importedRecordCompanies = new HashSet<>();

	@Persistent
	@RelatedTo(direction = Direction.OUTGOING, type = "IMPORTED_SHOW_BROADCASTS")
	private Set<ShowBroadcast> importedShowBroadcasts = new HashSet<>();

	@Persistent
	@RelatedTo(direction = Direction.OUTGOING, type = "IMPORTED_SONG_BROADCASTS")
	private Set<SongBroadcast> importedSongBroadcasts = new HashSet<>();

	@Persistent
	@RelatedTo(direction = Direction.OUTGOING, type = "IMPORTED_SONGS")
	private Set<Song> importedSongs = new HashSet<>();

	@Persistent
	@RelatedTo(direction = Direction.OUTGOING, type = "IMPORTED_STATIONS")
	private Set<Station> importedStations = new HashSet<>();

	private transient Map<String, String> map;

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

	public Iterable<String> getCategories() {
		if (map == null) {
			initializeCategories();
		}
		return map.keySet();
	}

	@RecordImportCategory("IMPORTED_ARTISTS")
	public Set<Artist> getImportedArtistList() {
		return importedArtists;
	}

	@RecordImportCategory("IMPORTED_CHART_POSITIONS")
	public List<ChartPosition> getImportedChartPositionList() {
		if (importedChartPositions != null) {
			return CollectionUtils.asList(importedChartPositions);
		} else {
			return Collections.emptyList();
		}
	}

	@RecordImportCategory("IMPORTED_CHART_STATES")
	public Set<ChartState> getImportedChartStateList() {
		return importedChartStates;
	}

	@RecordImportCategory("IMPORTED_PUBLISHERS")
	public Set<Publisher> getImportedPublisherList() {
		return importedPublishers;
	}

	@RecordImportCategory("IMPORTED_RECORD_COMPANIES")
	public Set<RecordCompany> getImportedRecordCompanyList() {
		return importedRecordCompanies;
	}

	public Collection<PersistentNode> getImportedRecords() {
		Collection<PersistentNode> importedRecords = new HashSet<>();
		for (String category : getCategories()) {
			importedRecords.addAll(getImportedRecords(category));
		}
		return importedRecords;
	}

	@SuppressWarnings("unchecked")
	public Collection<PersistentNode> getImportedRecords(String category) {
		if (map == null) {
			initializeCategories();
		}
		String methodName = map.get(category);
		if (methodName != null) {
			try {
				Object result = getClass().getMethod(methodName).invoke(this);
				if (result instanceof Collection) {
					return (Collection<PersistentNode>) result;
				}
			} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException exception) {
				exception.printStackTrace();
			}
		}
		return Collections.emptyList();
	}

	@RecordImportCategory("IMPORTED_SHOW_BROADCASTS")
	public List<ShowBroadcast> getImportedShowBroadcastList() {
		if (importedShowBroadcasts != null) {
			return CollectionUtils.asList(importedShowBroadcasts);
		} else {
			return Collections.emptyList();
		}
	}

	@RecordImportCategory("IMPORTED_SONG_BROADCASTS")
	public List<SongBroadcast> getImportedSongBroadcastList() {
		if (importedSongBroadcasts != null) {
			return CollectionUtils.asList(importedSongBroadcasts);
		} else {
			return Collections.emptyList();
		}
	}

	@RecordImportCategory("IMPORTED_SONGS")
	public Set<Song> getImportedSongList() {
		return importedSongs;
	}

	@RecordImportCategory("IMPORTED_STATIONS")
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

	public void importChartPosition(ChartPosition chartPosition) {
		importedChartPositions.add(chartPosition);
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

	private void initializeCategories() {
		map = new HashMap<>();
		Method[] methods = getClass().getMethods();
		for (Method method : methods) {
			if (Collection.class.isAssignableFrom(method.getReturnType()) && method.getParameterTypes().length == 0) {
				RecordImportCategory annotation = method.getAnnotation(RecordImportCategory.class);
				if (annotation != null) {
					map.put(annotation.value(), method.getName());
				}
			}
		}
	}

	private void writeObject(ObjectOutputStream outputStream) throws IOException {
		importedArtists = null;
		importedChartPositions = null;
		importedChartStates = null;
		importedPublishers = null;
		importedRecordCompanies = null;
		importedShowBroadcasts = null;
		importedSongBroadcasts = null;
		importedSongs = null;
		importedStations = null;
		outputStream.defaultWriteObject();
	}

}
