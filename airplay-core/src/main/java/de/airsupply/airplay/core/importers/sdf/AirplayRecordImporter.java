package de.airsupply.airplay.core.importers.sdf;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.text.StrTokenizer;
import org.slf4j.Logger;
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
import de.airsupply.airplay.core.model.SongBroadcast;
import de.airsupply.airplay.core.model.Station;
import de.airsupply.airplay.core.model.util.RecordImportProgressProvider;
import de.airsupply.airplay.core.services.ContentService;
import de.airsupply.commons.core.context.Loggable;

@Service
public class AirplayRecordImporter {

	@Loggable
	private Logger logger;

	public AirplayRecordImporter() {
		super();
	}

	private String identifySeparator(String line) {
		return StringUtils.split(line, ",").length > 1 ? "," : ";";
	}

	protected boolean mayImport() {
		return true;
	}

	@Autowired
	private ContentService contentService;

	private void processAirplayRecord(RecordImport recordImport, ChartState chartState, List<String> stationNames,
			StrTokenizer tokenizer, RecordImportProgressProvider progressProvider) {

		String[] tokens = tokenizer.getTokenArray();
		String artistName = tokens[56].trim();
		String songName = tokens[55].trim();
		String discIdentifier = tokens[59].trim();
		String recordCompanyName = tokens[57].trim();
		String publisherName = tokens[58].trim();
		String airplayChartPosition = tokens[0].trim();

		RecordCompany recordCompany = contentService.findOrCreate(new RecordCompany(recordCompanyName));
		Publisher publisher = contentService.findOrCreate(new Publisher(publisherName));
		Artist artist = contentService.findOrCreate(new Artist(artistName));
		Song song = contentService.findOrCreate(new Song(artist, songName, discIdentifier, recordCompany, publisher));

		recordImport.importRecordCompany(recordCompany);
		recordImport.importPublisher(publisher);
		recordImport.importArtist(artist);
		recordImport.importSong(song);

		for (int i = 0; i < stationNames.size(); i++) {
			Station station = contentService.findOrCreate(new Station(stationNames.get(i)));
			recordImport.importStation(station);

			int broadcastCount = Integer.valueOf(tokens[i + 62].trim()).intValue();
			if (broadcastCount > 0) {
				SongBroadcast songBroadcast = contentService.save(new SongBroadcast(station, song, recordImport
						.getWeekDate(), broadcastCount));
				recordImport.importBroadcast(songBroadcast);
				progressProvider.imported(songBroadcast);
			}
		}
		ChartPosition chartPosition = contentService.save(new ChartPosition(chartState, song, Integer.valueOf(
				airplayChartPosition).intValue()));
		recordImport.importChartPosition(chartPosition);
		progressProvider.imported(chartPosition);
	}

	private void processAirplayRecordHeader(List<String> stationNames, StrTokenizer tokenizer) {
		while (tokenizer.hasNext()) {
			String stationName = tokenizer.nextToken().trim();
			if (!stationName.isEmpty()) {
				stationNames.add(stationName);
			}
		}
	}

	public void processRecords(InputStream inputStream, ChartState chartState, RecordImport recordImport,
			RecordImportProgressProvider progressProvider) {
		Assert.notNull(inputStream);
		Assert.notNull(chartState);
		Assert.notNull(recordImport);
		Assert.notNull(progressProvider);

		progressProvider.reset();
		logger.info("Running import for week: " + recordImport.getWeekDate());

		BufferedReader reader = null;
		try {
			final List<String> stationNames = new ArrayList<>();
			List<String> lines = IOUtils.readLines(inputStream);
			IOUtils.closeQuietly(inputStream);

			progressProvider.setNumberOfRecords(lines.size() - 1);

			boolean firstLine = true;
			String separator = null;
			StrTokenizer tokenizer = StrTokenizer.getCSVInstance();
			for (String line : lines) {
				if (separator == null) {
					separator = identifySeparator(line);
				}
				tokenizer.reset(line);

				if (firstLine) {
					processAirplayRecordHeader(stationNames, tokenizer);
					firstLine = false;
				} else {
					processAirplayRecord(recordImport, chartState, stationNames, tokenizer, progressProvider);
					progressProvider.incrementIndex();
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

}
