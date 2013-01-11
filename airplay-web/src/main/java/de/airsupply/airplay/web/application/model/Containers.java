package de.airsupply.airplay.web.application.model;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.vaadin.data.util.BeanItemContainer;

import de.airsupply.airplay.core.model.Artist;
import de.airsupply.airplay.core.model.Chart;
import de.airsupply.airplay.core.model.ChartPosition;
import de.airsupply.airplay.core.model.Publisher;
import de.airsupply.airplay.core.model.RecordCompany;
import de.airsupply.airplay.core.model.RecordImport;
import de.airsupply.airplay.core.model.Show;
import de.airsupply.airplay.core.model.ShowBroadcast;
import de.airsupply.airplay.core.model.Song;
import de.airsupply.airplay.core.model.SongBroadcast;
import de.airsupply.airplay.core.services.ChartService;
import de.airsupply.airplay.core.services.ContentService;
import de.airsupply.airplay.core.services.ImportService;
import de.airsupply.airplay.core.services.StationService;

@SuppressWarnings("serial")
public class Containers implements Serializable {

	@Component
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public static class ArtistContainer extends BeanItemContainer<Artist> {

		@Autowired
		private transient ContentService contentService;

		public ArtistContainer() {
			super(Artist.class);
		}

		public void update() {
			removeAllItems();
			addAll(contentService.getArtists());
		}

		public void update(RecordImport recordImport) {
			removeAllItems();
			addAll(recordImport.getImportedArtistList());
		}

	}

	@Component
	public static class ChartContainer extends BeanItemContainer<Chart> {

		@Autowired
		private transient ChartService chartService;

		public ChartContainer() {
			super(Chart.class);
		}

		public void update() {
			addAll(chartService.getCharts());
		}

	}

	@Component
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public static class ChartPositionContainer extends BeanItemContainer<ChartPosition> {

		@Autowired
		private transient ChartService chartService;

		public ChartPositionContainer() {
			super(ChartPosition.class);
			addNestedContainerProperty("chartState.chart.name");
			addNestedContainerProperty("chartState.weekDate");
			addNestedContainerProperty("song.artist.name");
			addNestedContainerProperty("song.name");
		}

		public boolean update(Chart chart, Date date) {
			removeAllItems();
			addAll(chartService.findChartPositions(chart, date));
			return size() > 0;
		}

		public void update(Chart chart, Song song) {
			removeAllItems();
			addAll(chartService.findChartPositions(chart, song));
		}

		public void update(RecordImport recordImport) {
			removeAllItems();
			addAll(recordImport.getImportedChartPositionList());
		}

	}

	@Component
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public static class PublisherContainer extends BeanItemContainer<Publisher> {

		@Autowired
		private transient ContentService contentService;

		public PublisherContainer() {
			super(Publisher.class);
		}

		public void update() {
			removeAllItems();
			addAll(contentService.getPublishers());
		}

		public void update(RecordImport recordImport) {
			removeAllItems();
			addAll(recordImport.getImportedPublisherList());
		}

	}

	@Component
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public static class RecordCompanyContainer extends BeanItemContainer<RecordCompany> {

		@Autowired
		private transient ContentService contentService;

		public RecordCompanyContainer() {
			super(RecordCompany.class);
		}

		public void update() {
			removeAllItems();
			addAll(contentService.getRecordCompanies());
		}

		public void update(RecordImport recordImport) {
			removeAllItems();
			addAll(recordImport.getImportedRecordCompanyList());
		}

	}

	@Component
	public static class RecordImportContainer extends BeanItemContainer<RecordImport> {

		@Autowired
		private transient ImportService importService;

		public RecordImportContainer() {
			super(RecordImport.class);
		}

		public void update() {
			addAll(importService.getRecordImports());
		}

	}

	@Component
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public static class ShowBroadcastContainer extends BeanItemContainer<ShowBroadcast> {

		@Autowired
		private transient StationService stationService;

		public ShowBroadcastContainer() {
			super(ShowBroadcast.class);
			addNestedContainerProperty("station.name");
			addNestedContainerProperty("station.longName");
		}

		public void update(Show show) {
			removeAllItems();
			addAll(stationService.findBroadcasts(show));
		}

	}

	@Component
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public static class SongBroadcastContainer extends BeanItemContainer<SongBroadcast> {

		@Autowired
		private transient StationService stationService;

		public SongBroadcastContainer() {
			super(SongBroadcast.class);
			addNestedContainerProperty("station.name");
			addNestedContainerProperty("station.longName");
		}

		public void update(RecordImport recordImport) {
			removeAllItems();
			addAll(recordImport.getImportedSongBroadcastList());
		}

		public void update(Song song) {
			removeAllItems();
			addAll(stationService.findBroadcasts(song));
		}

	}

	@Component
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public static class SongContainer extends BeanItemContainer<Song> {

		@Autowired
		private transient ContentService contentService;

		public SongContainer() {
			super(Song.class);
			addNestedContainerProperty("artist.name");
		}

		public boolean search(String query, boolean advancedSearch) {
			if (StringUtils.hasText(query)) {
				removeAllFilters();
				removeAllItems();
				List<Song> songs = contentService.findSongs(query, advancedSearch);
				addAll(songs);
				return !songs.isEmpty();
			} else {
				filterAll();
				return false;
			}
		}

		public void update() {
			removeAllItems();
			addAll(contentService.getSongs());
		}

		public void update(Artist artist) {
			removeAllItems();
			addAll(contentService.findSongs(artist));
		}

		public void update(RecordImport recordImport) {
			removeAllItems();
			addAll(recordImport.getImportedSongList());
		}

	}

	private Containers() {
		super();
	}

}
