package de.airsupply.airplay.core.importers.sdf;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.text.StrTokenizer;
import org.slf4j.Logger;
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
import de.airsupply.commons.core.context.Loggable;

@Service
public class AirplayRecordImporter {

	private static class CacheContext {

		private Map<Class<?>, NameCache<?>> map = new HashMap<>();

		@SuppressWarnings({ "unchecked" })
		public <T> NameCache<T> getCache(Class<T> key) {
			NameCache<?> cache = map.get(key);
			if (cache == null) {
				cache = new DefaultNameCache<T>(key);
				map.put(key, cache);
			}
			return (NameCache<T>) cache;
		}

	}

	private static class DefaultNameCache<T> extends NameCache<T> {

		private Class<T> type;

		public DefaultNameCache(Class<T> type) {
			this.type = type;
		}

		@Override
		protected T create(String name) {
			try {
				return type.getConstructor(String.class).newInstance(name);
			} catch (Exception exception) {
				return null;
			}
		}

	}

	private static abstract class NameCache<T> {

		private Map<String, T> map = new HashMap<>();

		protected abstract T create(String name);

		public T get(String name) {
			T value = map.get(name);
			if (value == null) {
				value = create(name);
				Assert.notNull(value);
				map.put(name, value);
			}
			return value;
		}

	}

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

	private void processAirplayRecord(RecordImport recordImport, ChartState chartState, List<String> stationNames,
			StrTokenizer tokenizer, CacheContext context) {

		String[] tokens = tokenizer.getTokenArray();
		String artistName = tokens[56].trim();
		String songName = tokens[55].trim();
		String discIdentifier = tokens[59].trim();
		String recordCompanyName = tokens[57].trim();
		String publisherName = tokens[58].trim();
		String airplayChartPosition = tokens[0].trim();

		RecordCompany recordCompany = context.getCache(RecordCompany.class).get(recordCompanyName);
		Publisher publisher = context.getCache(Publisher.class).get(publisherName);
		Artist artist = context.getCache(Artist.class).get(artistName);
		Song song = new Song(artist, songName, discIdentifier, recordCompany, publisher);

		recordImport.importRecordCompany(recordCompany);
		recordImport.importPublisher(publisher);
		recordImport.importArtist(artist);
		recordImport.importSong(song);

		for (int i = 0; i < stationNames.size(); i++) {
			Station station = context.getCache(Station.class).get(stationNames.get(i));
			recordImport.importStation(station);

			int broadcastCount = Integer.valueOf(tokens[i + 62].trim()).intValue();
			if (broadcastCount > 0) {
				recordImport.importBroadcast(new SongBroadcast(station, song, recordImport.getWeekDate(),
						broadcastCount));
			}
		}

		ChartPosition chartPosition = new ChartPosition(chartState, song, Integer.valueOf(airplayChartPosition)
				.intValue());
		recordImport.importChartPosition(chartPosition);
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

		logger.info("Running import for week: " + recordImport.getWeekDate());

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
					processAirplayRecord(recordImport, chartState, stationNames, tokenizer, new CacheContext());
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
