package de.airsupply.airplay.core.importers.dbf;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
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
import de.airsupply.airplay.core.model.RecordCompany;
import de.airsupply.airplay.core.model.Song;
import de.airsupply.airplay.core.model.SongBroadcast;
import de.airsupply.airplay.core.model.Station;
import de.airsupply.commons.core.context.Loggable;
import de.airsupply.commons.core.dbf.DBFReader;
import de.airsupply.commons.core.dbf.DBFReader.RecordHandler;
import de.airsupply.commons.core.neo4j.Neo4jBatchInserter;
import de.airsupply.commons.core.util.DateUtils;

@Service
public class DBFImporter {

	private static final String DATABASE_FILE_ARCHIVE = "AM_ARCH.DBF";

	private static final String DATABASE_FILE_STATE = "AM_BEWE.DBF";

	private static final String DATABASE_FILE_STATIONS = "AM_SEND.DBF";

	private static final String STATE_TYPE_AIRPLAY_CHART = "C";

	private static final String STATE_TYPE_SALES_CHART = "D";

	private static final String STATE_TYPE_SONG_BROADCAST_EXACT = "A";

	private static final String STATE_TYPE_SONG_BROADCAST_WEEKLY = "B";

	private DBFImporterContext context;

	@Loggable
	private Logger logger;

	@Autowired
	private Neo4jBatchInserter neo4jBatchInserter;

	private Map<Integer, Song> songMap = new HashMap<>(30000);

	public void migrate(String fileDirectory, String storeDirectory) {
		context = new DBFImporterContext(new ArrayList<>(5200000));

		migrateStations(fileDirectory);
		migrateSongs(fileDirectory);
		migrateStates(fileDirectory);

		List<Object> objects = context.getObjects();

		context = null;
		songMap = null;

		neo4jBatchInserter.runBatch(storeDirectory, objects);
	}

	@Transactional
	private void migrateSongs(String fileDirectory) {
		logger.info("Migrating songs and artists.");

		File file = new File(fileDirectory, DATABASE_FILE_ARCHIVE);
		NumberRange recordsToSkip = new NumberRange(Integer.valueOf(5372), Integer.valueOf(7285));
		for (Record record : DBFReader.readRecords(file, true, recordsToSkip)) {
			String artistName = record.getStringValue("INTE").trim();
			String songIdentifier = record.getStringValue("ARNR").trim();
			String songName = record.getStringValue("TITL").trim();
			String recordCompanyName = record.getStringValue("FIKU").trim();
			String discIdentifier = record.getStringValue("PLNR").trim();

			if (StringUtils.hasText(artistName) && StringUtils.hasText(songName)) {
				Artist artist = context.getOrPersist(new Artist(artistName));
				RecordCompany recordCompany = null;
				if (StringUtils.hasText(recordCompanyName)) {
					recordCompany = context.getOrPersist(new RecordCompany(recordCompanyName));
				}
				Song song = context.getOrPersist(new Song(artist, songName, discIdentifier, recordCompany, null));
				songMap.put(Integer.valueOf(songIdentifier), song);
			}
		}
	}

	private void migrateStateOfChartPosition(Chart chart, Record record, Table table) {
		String songIdentifier = record.getStringValue("ARNR").trim();
		String chartStateDate = record.getStringValue("DATE").trim();
		String chartPositionIndex = record.getStringValue("TIME").trim();

		Song song = songMap.get(Integer.valueOf(songIdentifier));

		Date date = DateUtils.getStartOfWeek(parseDateFormat(chartStateDate));
		ChartState chartState = context.get(new ChartState(chart, date));

		Assert.notNull(song);
		Assert.notNull(chartState);

		context.persist(new ChartPosition(chartState, song, Integer.valueOf(chartPositionIndex).intValue()));
	}

	private void migrateStateOfChartState(Chart chart, Record record, Table table) {
		String chartStateDate = record.getStringValue("DATE").trim();
		Date date = DateUtils.getStartOfWeek(parseDateFormat(chartStateDate));
		context.getOrPersist(new ChartState(chart, date));
	}

	private void migrateStateOfSongBroadcast(Record record, Table table, boolean exact) {
		String songIdentifier = record.getStringValue("ARNR").trim();
		String stationName = record.getStringValue("SEND").trim();
		String broadcastDate = record.getStringValue("DATE").trim();
		String broadcastTime = record.getStringValue("TIME").trim();
		Number count = record.getNumberValue("ZAHL");

		Station station = context.getOrPersist(new Station(stationName, null));
		Song song = songMap.get(Integer.valueOf(songIdentifier));

		Assert.notNull(station);
		Assert.notNull(song);

		if (exact) {
			Date date = parseDateFormat(broadcastDate, broadcastTime);
			context.getOrPersist(new SongBroadcast(station, song, date));
		} else {
			Date date = DateUtils.getStartOfWeek(parseDateFormat(broadcastDate));
			context.getOrPersist(new SongBroadcast(station, song, date, count.intValue()));
		}
	}

	private void migrateStates(String fileDirectory) {
		logger.info("Migrating States.");

		final Chart airplayChart = context.persist(new Chart("Airplay Charts"));
		final Chart salesChart = context.persist(new Chart("Sales Charts"));

		File file = new File(fileDirectory, DATABASE_FILE_STATE);
		DBFReader.processRecords(file, new RecordHandler() {

			@Override
			public void handle(Record record, Table table, int index, boolean isLast) {
				switch (record.getStringValue("ART")) {
				case STATE_TYPE_AIRPLAY_CHART:
					migrateStateOfChartState(airplayChart, record, table);
					migrateStateOfChartPosition(airplayChart, record, table);
					break;
				case STATE_TYPE_SALES_CHART:
					migrateStateOfChartState(salesChart, record, table);
					migrateStateOfChartPosition(salesChart, record, table);
					break;
				case STATE_TYPE_SONG_BROADCAST_EXACT:
					migrateStateOfSongBroadcast(record, table, true);
					break;
				case STATE_TYPE_SONG_BROADCAST_WEEKLY:
					migrateStateOfSongBroadcast(record, table, false);
					break;
				default:
					break;
				}
			}

		});
	}

	@Transactional
	private void migrateStations(String fileDirectory) {
		logger.info("Migrating stations.");

		File file = new File(fileDirectory, DATABASE_FILE_STATIONS);
		List<Record> records = DBFReader.readRecords(file, false, null);
		for (Record record : records) {
			String name = record.getStringValue("NAME").trim();
			String longName = record.getStringValue("BEZE").trim();
			context.getOrPersist(new Station(name, longName));
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