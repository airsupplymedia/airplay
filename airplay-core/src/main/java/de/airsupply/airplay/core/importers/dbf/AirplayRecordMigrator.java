package de.airsupply.airplay.core.importers.dbf;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nl.knaw.dans.common.dbflib.Record;
import nl.knaw.dans.common.dbflib.Table;

import org.apache.commons.lang.math.NumberRange;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import de.airsupply.airplay.core.model.Artist;
import de.airsupply.airplay.core.model.Chart;
import de.airsupply.airplay.core.model.ChartPosition;
import de.airsupply.airplay.core.model.ChartState;
import de.airsupply.airplay.core.model.PersistentNode;
import de.airsupply.airplay.core.model.RecordCompany;
import de.airsupply.airplay.core.model.Song;
import de.airsupply.airplay.core.model.SongBroadcast;
import de.airsupply.airplay.core.model.Station;
import de.airsupply.airplay.core.services.ChartService;
import de.airsupply.airplay.core.services.ContentService;
import de.airsupply.airplay.core.services.StationService;
import de.airsupply.commons.core.context.Loggable;
import de.airsupply.commons.core.dbf.DBFReader;
import de.airsupply.commons.core.dbf.DBFReader.RecordHandler;
import de.airsupply.commons.core.util.DateUtils;
import de.airsupply.commons.core.util.Pair;

@Service
public class AirplayRecordMigrator {

	private static final String DATABASE_FILE_ARCHIVE = "AM_ARCH.DBF";

	private static final String DATABASE_FILE_STATE = "AM_BEWE.DBF";

	private static final String DATABASE_FILE_STATIONS = "AM_SEND.DBF";

	private static final String STATE_TYPE_AIRPLAY_CHART = "C";

	private static final String STATE_TYPE_SALES_CHART = "D";

	private static final String STATE_TYPE_SONG_BROADCAST_EXACT = "A";

	private static final String STATE_TYPE_SONG_BROADCAST_WEEKLY = "B";

	@Autowired
	private ChartService chartService;

	private Map<Pair<String, Chart>, ChartState> chartStateMap = new HashMap<>();

	@Autowired
	private ContentService contentService;

	private final String fileDirectory = "C:\\Development\\Storage\\Git\\airplay\\airplay-dbf";

	@Loggable
	private Logger logger;

	private Map<Integer, Song> songMap = new HashMap<>(30000);

	private Map<String, Station> stationMap = new HashMap<>(300);

	@Autowired
	private StationService stationService;

	public AirplayRecordMigrator() {
		super();
	}

	public void migrate() {
		migrateStations();
		migrateSongs();
		migrateStates();
	}

	@Transactional
	private void migrateSongs() {
		logger.info("Migrating songs and artists.");

		File file = new File(fileDirectory, DATABASE_FILE_ARCHIVE);
		NumberRange recordsToSkip = new NumberRange(Integer.valueOf(5372), Integer.valueOf(7285));
		List<Record> records = DBFReader.readRecords(file, true, recordsToSkip);
		for (Record record : records) {
			String artistName = record.getStringValue("INTE").trim();
			String songIdentifier = record.getStringValue("ARNR").trim();
			String songName = record.getStringValue("TITL").trim();
			String recordCompanyName = record.getStringValue("FIKU").trim();
			String discIdentifier = record.getStringValue("PLNR").trim();

			if (StringUtils.hasText(artistName) && StringUtils.hasText(songName)) {
				Artist artist = contentService.findOrCreate(new Artist(artistName));
				RecordCompany recordCompany = null;
				if (StringUtils.hasText(recordCompanyName)) {
					recordCompany = contentService.findOrCreate(new RecordCompany(recordCompanyName));
				}
				Song song = contentService
						.findOrCreate(new Song(artist, songName, discIdentifier, recordCompany, null));
				songMap.put(Integer.valueOf(songIdentifier), song);
			}
		}
	}

	private ChartPosition migrateStateOfChartPosition(Chart chart, Record record, Table table) {
		String songIdentifier = record.getStringValue("ARNR").trim();
		String chartStateDate = record.getStringValue("DATE").trim();
		String chartPositionIndex = record.getStringValue("TIME").trim();

		Song song = songMap.get(Integer.valueOf(songIdentifier));

		Pair<String, Chart> key = new Pair<>(chartStateDate, chart);
		ChartState chartState = chartStateMap.get(key);

		Assert.notNull(song);
		Assert.notNull(chartState);

		return new ChartPosition(chartState, song, Integer.valueOf(chartPositionIndex).intValue());
	}

	private void migrateStateOfChartState(Chart chart, Record record, Table table) {
		String chartStateDate = record.getStringValue("DATE").trim();

		Pair<String, Chart> key = new Pair<>(chartStateDate, chart);
		if (!chartStateMap.containsKey(key)) {
			Date date = DateUtils.getStartOfWeek(parseDateFormat(chartStateDate));
			ChartState chartState = chartService.save(new ChartState(chart, date));
			chartStateMap.put(key, chartState);
		}
	}

	private SongBroadcast migrateStateOfSongBroadcast(Record record, Table table, boolean exact) {
		String songIdentifier = record.getStringValue("ARNR").trim();
		String stationName = record.getStringValue("SEND").trim();
		String broadcastDate = record.getStringValue("DATE").trim();
		String broadcastTime = record.getStringValue("TIME").trim();
		Number count = record.getNumberValue("ZAHL");

		Station station = stationMap.get(stationName);
		if (station == null) {
			station = stationService.save(new Station(stationName, null));
			stationMap.put(stationName, station);
		}
		Song song = songMap.get(Integer.valueOf(songIdentifier));

		Assert.notNull(station);
		Assert.notNull(song);

		if (exact) {
			Date date = parseDateFormat(broadcastDate, broadcastTime);
			return new SongBroadcast(station, song, date);
		} else {
			Date date = DateUtils.getStartOfWeek(parseDateFormat(broadcastDate));
			return new SongBroadcast(station, song, date, count.intValue());
		}
	}

	private void migrateStates() {
		logger.info("Migrating States.");

		final Chart airplayChart = chartService.findOrCreate(new Chart("Airplay Charts"));
		final Chart salesChart = chartService.findOrCreate(new Chart("Sales Charts"));

		final int interval = 50000;
		final List<PersistentNode> states = new ArrayList<>(interval);

		File file = new File(fileDirectory, DATABASE_FILE_STATE);
		DBFReader.processRecords(file, new RecordHandler() {

			@Override
			public void handle(Record record, Table table, int index, boolean isLast) {
				if (states.size() >= interval) {
					migrateStates(states);
				}

				switch (record.getStringValue("ART")) {
				case STATE_TYPE_AIRPLAY_CHART:
					migrateStateOfChartState(airplayChart, record, table);
					states.add(migrateStateOfChartPosition(airplayChart, record, table));
					break;
				case STATE_TYPE_SALES_CHART:
					migrateStateOfChartState(salesChart, record, table);
					states.add(migrateStateOfChartPosition(salesChart, record, table));
					break;
				case STATE_TYPE_SONG_BROADCAST_EXACT:
					states.add(migrateStateOfSongBroadcast(record, table, true));
					break;
				case STATE_TYPE_SONG_BROADCAST_WEEKLY:
					states.add(migrateStateOfSongBroadcast(record, table, false));
					break;
				default:
					break;
				}

				if (isLast) {
					migrateStates(states);
				}
			}

		});
	}

	@Transactional
	private void migrateStates(Collection<PersistentNode> states) {
		logger.info("Migrating " + states.size() + " states");
		for (PersistentNode state : states) {
			try {
				chartService.save(state);
			} catch (Exception exception) {
				logger.error("Error migrating state: " + state, exception);
			}
		}
		states.clear();
	}

	@Transactional
	private void migrateStations() {
		logger.info("Migrating stations.");

		File file = new File(fileDirectory, DATABASE_FILE_STATIONS);
		List<Record> records = DBFReader.readRecords(file, false, null);
		for (Record record : records) {
			String name = record.getStringValue("NAME").trim();
			String longName = record.getStringValue("BEZE").trim();

			Station station = stationService.findOrCreate(new Station(name, longName));
			stationMap.put(name, station);
		}
	}

	private Date parseDateFormat(String date) {
		return parseDateFormat(date, null);
	}

	private Date parseDateFormat(String date, String time) {
		int year = Integer.valueOf(date.substring(0, 4)).intValue();
		int month = Integer.valueOf(date.substring(4, 6)).intValue() - 1;
		int day = Integer.valueOf(date.substring(6, 8)).intValue();

		int hour = 0;
		int minute = 0;
		if (StringUtils.hasText(time)) {
			hour = Integer.valueOf(time.substring(0, 2)).intValue();
			minute = Integer.valueOf(time.substring(2, 4)).intValue();
		}

		Calendar calendar = new GregorianCalendar(year, month, day, hour, minute);
		return calendar.getTime();
	}

}