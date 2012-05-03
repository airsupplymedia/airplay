package de.airsupply.airplay.core.importers.sdf;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.text.StrTokenizer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import de.airsupply.airplay.core.model.Artist;
import de.airsupply.airplay.core.model.ChartPosition;
import de.airsupply.airplay.core.model.ChartState;
import de.airsupply.airplay.core.model.Publisher;
import de.airsupply.airplay.core.model.RecordCompany;
import de.airsupply.airplay.core.model.RecordImport;
import de.airsupply.airplay.core.model.Song;
import de.airsupply.airplay.core.model.Station;
import de.airsupply.airplay.core.services.ChartService;
import de.airsupply.airplay.core.services.ContentService;
import de.airsupply.airplay.core.services.StationService;

@Service
public class AirplayRecordImporter {

	@Autowired
	private ChartService chartService;

	@Autowired
	private ContentService contentService;

	private Log log = LogFactory.getLog(getClass());

	@Autowired
	private StationService stationService;

	public AirplayRecordImporter() {
		super();
	}

	private String identifySeparator(String line) {
		return StringUtils.split(line, ",").length > 1 ? "," : ";";
	}

	protected boolean mayImport() {
		return true;
	}

	private void processAirplayRecord(RecordImport recordImport, ChartState chartState, List<String> stationNames,
			StrTokenizer tokenizer) {
		Date week = recordImport.getWeekDate();

		String[] tokens = tokenizer.getTokenArray();
		String artistName = tokens[56].trim();
		String songName = tokens[55].trim();
		String discIdentifier = tokens[59].trim();
		String recordCompanyName = tokens[57].trim();
		String publisherName = tokens[58].trim();
		String airplayChartPosition = tokens[0].trim();

		contentService.find(new Artist(artistName));

		// Artist artist = contentService.findOrCreate(new Artist(artistName));
		// Publisher publisher = contentService.findOrCreate(new
		// Publisher(publisherName));
		// RecordCompany recordCompany = contentService.findOrCreate(new
		// RecordCompany(recordCompanyName));
		// Song song = contentService.findOrCreate(new Song(artist, songName,
		// discIdentifier, recordCompany, publisher));

		RecordCompany recordCompany = new RecordCompany(recordCompanyName);
		Publisher publisher = new Publisher(publisherName);
		Artist artist = new Artist(artistName);
		Song song = new Song(artist, songName, discIdentifier, recordCompany, publisher);

		recordImport.importRecordCompany(recordCompany);
		recordImport.importPublisher(publisher);
		recordImport.importArtist(artist);
		recordImport.importSong(song);

		for (int i = 0; i < stationNames.size(); i++) {
			// Station station = stationService.findOrCreate(new
			// Station(stationNames.get(i), null));
			recordImport.importStation(new Station(stationNames.get(i), null));

			int broadcastCount = Integer.valueOf(tokens[i + 62].trim()).intValue();

			// if (broadcastCount > 0) {
			// SongBroadcast songBroadcast = new SongBroadcast(station, song,
			// week, broadcastCount);
			// Assert.isNull(stationService.find(songBroadcast),
			// "Broadcasts may not be imported twice!");
			// songBroadcast = stationService.save(songBroadcast);
			// recordImport.importBroadcast(songBroadcast);
			// }
		}

		ChartPosition chartPosition = chartService.findOrCreate(new ChartPosition(chartState, song, Integer.valueOf(
				airplayChartPosition).intValue()));
		log.info("Importing: " + chartPosition);
	}

	private void processAirplayRecordHeader(List<String> stationNames, StrTokenizer tokenizer) {
		while (tokenizer.hasNext()) {
			String stationName = tokenizer.nextToken().trim();
			if (!stationName.isEmpty()) {
				stationNames.add(stationName);
			}
		}
	}

	public void processRecords(InputStream inputStream, ChartState chartState, RecordImport recordImport) {
		Assert.notNull(inputStream);
		Assert.notNull(chartState);
		Assert.notNull(recordImport);

		log.info("Running import for week: " + recordImport.getWeekDate());

		BufferedReader reader = null;
		try {
			final List<String> stationNames = new ArrayList<>();

			reader = new BufferedReader(new InputStreamReader(inputStream));
			boolean firstLine = true;
			String separator = null;
			StrTokenizer tokenizer = StrTokenizer.getCSVInstance();

			while (reader.ready()) {
				String line = reader.readLine();
				if (separator == null) {
					separator = identifySeparator(line);
				}
				tokenizer.reset(line);

				if (firstLine) {
					processAirplayRecordHeader(stationNames, tokenizer);
					firstLine = false;
				} else {
					processAirplayRecord(recordImport, chartState, stationNames, tokenizer);
				}
			}
			recordImport.importChartState(chartState);
		} catch (IOException exception) {
			throw new DataRetrievalFailureException("Data cant't be imported!", exception);
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException exception) {
					exception.printStackTrace();
				}
			}
		}
	}

	public void commitRecordImport(RecordImport recordImport) {
		Assert.notNull(recordImport);

		contentService.findOrCreate(recordImport.getImportedPublisherList());
		contentService.findOrCreate(recordImport.getImportedRecordCompanyList());
		contentService.findOrCreate(recordImport.getImportedArtistList());
		contentService.findOrCreate(recordImport.getImportedSongList());
		stationService.findOrCreate(recordImport.getImportedStationList());
		stationService.findOrCreate(recordImport.getImportedSongBroadcastList());
		stationService.findOrCreate(recordImport.getImportedShowBroadcastList());
	}

}
