package de.airsupply.airplay.core.importers.dbf;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nl.knaw.dans.common.dbflib.Record;
import nl.knaw.dans.common.dbflib.Table;

import org.apache.commons.lang.math.NumberRange;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import de.airsupply.airplay.core.model.Artist;
import de.airsupply.airplay.core.model.Chart;
import de.airsupply.airplay.core.model.ChartPosition;
import de.airsupply.airplay.core.model.ChartState;
import de.airsupply.airplay.core.model.RecordCompany;
import de.airsupply.airplay.core.model.Song;
import de.airsupply.airplay.core.model.SongBroadcast;
import de.airsupply.airplay.core.model.Station;
import de.airsupply.airplay.core.services.ChartService;
import de.airsupply.airplay.core.services.ContentService;
import de.airsupply.airplay.core.services.StationService;
import de.airsupply.commons.core.dbf.DBFReader;
import de.airsupply.commons.core.dbf.DBFReader.RecordHandler;
import de.airsupply.commons.core.util.DateUtils;
import de.airsupply.commons.core.util.Pair;

@Service
public class AirplayLegacyMigrator {

	private static final String DATABASE_FILE_ARCHIVE = "AM_ARCH.DBF";

	private static final String DATABASE_FILE_STATE = "AM_BEWE.DBF";

	private static final String DATABASE_FILE_STATIONS = "AM_SEND.DBF";

	private static final String STATE_TYPE_AIRPLAY_CHART = "C";

	private static final String STATE_TYPE_MEDIA_CONTROL_CHART = "D";

	private static final String STATE_TYPE_SONG_BROADCAST_EXACT = "A";

	private static final String STATE_TYPE_SONG_BROADCAST_WEEKLY = "B";

	@Autowired
	private ChartService chartService;

	private Map<Pair<String, Chart>, ChartState> chartStateMap = new HashMap<>();

	@Autowired
	private ContentService contentService;

	private final String fileDirectory = "C:\\Development\\Projects\\ASM\\Application\\airplay-dbf";

	private final Log log = LogFactory.getLog(getClass());

	private Map<Integer, Song> songMap = new HashMap<>(30000);

	private Map<String, Station> stationMap = new HashMap<>(300);

	@Autowired
	private StationService stationService;

	public AirplayLegacyMigrator() {
		super();
	}

	private void migrateChartPosition(Chart chart, Record record, Table table) {
		String songIdentifier = record.getStringValue("ARNR").trim();
		String chartStateDate = record.getStringValue("DATE").trim();
		String chartPositionIndex = record.getStringValue("TIME").trim();

		Song song = songMap.get(Integer.valueOf(songIdentifier));

		Pair<String, Chart> key = new Pair<>(chartStateDate, chart);
		ChartState chartState = chartStateMap.get(key);

		Assert.notNull(song);
		Assert.notNull(chartState);
		if (chartState != null) {
			Date date = DateUtils.getStartOfWeek(parseDateFormat(chartStateDate));
			ChartPosition chartPosition = chartService.findOrCreate(new ChartPosition(chartState, song, Integer
					.valueOf(chartPositionIndex).intValue()));
			log.info("Importing: " + "[" + chartPositionIndex + "] - " + chartPosition + " ["
					+ DateUtils.getWeekOfYearFormat(date) + "]");
		}
	}

	@Transactional
	public void migrateChartPositions() {
		log.info("Migrating Chart positions.");

		final Chart airplayChart = chartService.findOrCreate(new Chart("Airplay Charts"));
		final Chart mediaControlChart = chartService.findOrCreate(new Chart("Media Control Charts"));

		File file = new File(fileDirectory, DATABASE_FILE_STATE);
		DBFReader.processRecords(file, new RecordHandler() {

			@Override
			public void handle(Record record, Table table) {
				String stateType = record.getStringValue("ART");
				switch (stateType) {
				case STATE_TYPE_AIRPLAY_CHART:
					migrateChartPosition(airplayChart, record, table);
					break;
				case STATE_TYPE_MEDIA_CONTROL_CHART:
					migrateChartPosition(mediaControlChart, record, table);
					break;
				default:
					break;
				}
			}

		});
	}

	private void migrateChartState(Chart chart, Record record, Table table) {
		String chartStateDate = record.getStringValue("DATE").trim();

		Pair<String, Chart> key = new Pair<>(chartStateDate, chart);
		if (!chartStateMap.containsKey(key)) {
			Date date = DateUtils.getStartOfWeek(parseDateFormat(chartStateDate));
			ChartState chartState = chartService.findOrCreate(new ChartState(chart, date));
			chartStateMap.put(key, chartState);
			log.info("Importing: " + chartState + " [" + DateUtils.getWeekOfYearFormat(date) + "]");
		}
	}

	@Transactional
	public void migrateChartStates() {
		log.info("Migrating Chart states.");

		final Chart airplayChart = chartService.findOrCreate(new Chart("Airplay Charts"));
		final Chart mediaControlChart = chartService.findOrCreate(new Chart("Media Control Charts"));

		File file = new File(fileDirectory, DATABASE_FILE_STATE);
		DBFReader.processRecords(file, new RecordHandler() {

			@Override
			public void handle(Record record, Table table) {
				String stateType = record.getStringValue("ART");
				switch (stateType) {
				case STATE_TYPE_AIRPLAY_CHART:
					migrateChartState(airplayChart, record, table);
					break;
				case STATE_TYPE_MEDIA_CONTROL_CHART:
					migrateChartState(mediaControlChart, record, table);
					break;
				default:
					break;
				}
			}

		});
	}

	@Transactional
	public void migrateSongBroadcast(Record record, Table table, boolean exact) {
		String songIdentifier = record.getStringValue("ARNR").trim();
		String stationName = record.getStringValue("SEND").trim();
		String broadcastDate = record.getStringValue("DATE").trim();
		String broadcastTime = record.getStringValue("TIME").trim();
		Number count = record.getNumberValue("ZAHL");

		Station station = stationMap.get(stationName);
		if (station == null) {
			station = stationService.findOrCreate(new Station(stationName, null));
			stationMap.put(stationName, station);
		}
		Song song = songMap.get(Integer.valueOf(songIdentifier));

		Assert.notNull(station);
		Assert.notNull(song);

		SongBroadcast songBroadcast = null;
		if (exact) {
			Date date = parseDateFormat(broadcastDate, broadcastTime);
			songBroadcast = stationService.findOrCreate(new SongBroadcast(station, song, date));
		} else {
			Date date = DateUtils.getStartOfWeek(parseDateFormat(broadcastDate));
			songBroadcast = stationService.findOrCreate(new SongBroadcast(station, song, date, count.intValue()));
		}
		log.info("Importing: " + songBroadcast);
	}

	public void migrateSongBroadcasts() {
		log.info("Migrating Song Broadcasts.");

		File file = new File(fileDirectory, DATABASE_FILE_STATE);
		DBFReader.processRecords(file, new RecordHandler() {

			@Override
			public void handle(Record record, Table table) {
				String stateType = record.getStringValue("ART");
				switch (stateType) {
				case STATE_TYPE_SONG_BROADCAST_EXACT:
					migrateSongBroadcast(record, table, true);
					break;
				case STATE_TYPE_SONG_BROADCAST_WEEKLY:
					migrateSongBroadcast(record, table, false);
					break;
				default:
					break;
				}
			}

		});
	}

	@Transactional
	public void migrateSongs() {
		log.info("Migrating songs and artists.");

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
				log.info("Imported: " + song);
			}
		}
	}

	@Transactional
	public void migrateStations() {
		log.info("Migrating stations.");

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
