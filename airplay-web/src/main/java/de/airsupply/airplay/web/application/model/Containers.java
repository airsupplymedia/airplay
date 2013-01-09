package de.airsupply.airplay.web.application.model;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.vaadin.data.util.BeanItemContainer;

import de.airsupply.airplay.core.model.Chart;
import de.airsupply.airplay.core.model.ChartPosition;
import de.airsupply.airplay.core.model.Show;
import de.airsupply.airplay.core.model.ShowBroadcast;
import de.airsupply.airplay.core.model.Song;
import de.airsupply.airplay.core.model.SongBroadcast;
import de.airsupply.airplay.core.services.ChartService;
import de.airsupply.airplay.core.services.ContentService;
import de.airsupply.airplay.core.services.StationService;

@SuppressWarnings("serial")
public class Containers implements Serializable {

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
	public static class ChartPositionContainer extends BeanItemContainer<ChartPosition> {

		@Autowired
		private transient ChartService chartService;

		public ChartPositionContainer() {
			super(ChartPosition.class);
			addNestedContainerProperty("song.artist.name");
			addNestedContainerProperty("song.name");
		}

		public boolean update(Chart chart, Date date) {
			removeAllItems();
			List<ChartPosition> chartPositions = chartService.findChartPositions(chart, date);
			addAll(chartPositions);
			return !chartPositions.isEmpty();
		}

	}

	@Component
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
	public static class SongBroadcastContainer extends BeanItemContainer<SongBroadcast> {

		@Autowired
		private transient StationService stationService;

		public SongBroadcastContainer() {
			super(SongBroadcast.class);
			addNestedContainerProperty("station.name");
			addNestedContainerProperty("station.longName");
		}

		public void update(Song song) {
			removeAllItems();
			addAll(stationService.findBroadcasts(song));
		}

	}

	@Component
	public static class SongChartPositionContainer extends BeanItemContainer<ChartPosition> {

		@Autowired
		private transient ChartService chartService;

		public SongChartPositionContainer() {
			super(ChartPosition.class);
			addNestedContainerProperty("chartState.weekDate");
		}

		public void update(Chart chart, Song song) {
			removeAllItems();
			addAll(chartService.findChartPositions(chart, song));
		}

	}

	@Component
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

	}

	private Containers() {
		super();
	}

}
