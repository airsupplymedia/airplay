package de.airsupply.airplay.web.application.model;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
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

@Service
@SuppressWarnings("serial")
public class AirplayDataProvider implements Serializable {

	public class ChartContainer extends BeanItemContainer<Chart> {

		public ChartContainer() {
			super(Chart.class);
			addAll(getChartService().getCharts());
		}

	}

	public class ChartPositionContainer extends BeanItemContainer<ChartPosition> {

		public ChartPositionContainer() {
			super(ChartPosition.class);
			addNestedContainerProperty("song.artist.name");
			addNestedContainerProperty("song.name");
		}

		public boolean update(Chart chart, Date date) {
			removeAllItems();
			List<ChartPosition> chartPositions = getChartService().findChartPositions(chart, date);
			addAll(chartPositions);
			return !chartPositions.isEmpty();
		}

	}

	public class ShowBroadcastContainer extends BeanItemContainer<ShowBroadcast> {

		public ShowBroadcastContainer() {
			super(ShowBroadcast.class);
			addNestedContainerProperty("station.name");
		}

		public void update(Show show) {
			removeAllItems();
			addAll(getStationService().findBroadcasts(show));
		}

	}

	public class SongBroadcastContainer extends BeanItemContainer<SongBroadcast> {

		public SongBroadcastContainer() {
			super(SongBroadcast.class);
			addNestedContainerProperty("station.name");
		}

		public void update(Song song) {
			removeAllItems();
			addAll(getStationService().findBroadcasts(song));
		}

	}

	public class SongChartPositionContainer extends BeanItemContainer<ChartPosition> {

		public SongChartPositionContainer() {
			super(ChartPosition.class);
			addNestedContainerProperty("chartState.weekDate");
		}

		public void update(Chart chart, Song song) {
			removeAllItems();
			addAll(getChartService().findChartPositions(chart, song));
		}

	}

	public class SongContainer extends BeanItemContainer<Song> {

		public SongContainer() {
			super(Song.class);
			addNestedContainerProperty("artist.name");
		}

		public boolean search(String query, boolean advancedSearch) {
			if (StringUtils.hasText(query)) {
				removeAllFilters();
				removeAllItems();
				List<Song> songs = getContentService().findSongs(query, advancedSearch);
				addAll(songs);
				return !songs.isEmpty();
			} else {
				filterAll();
				return false;
			}
		}

	}

	@Autowired
	private transient ChartService chartService;

	@Autowired
	private transient ContentService contentService;

	@Autowired
	private transient StationService stationService;

	public ChartContainer createChartContainer() {
		return new ChartContainer();
	}

	public ChartPositionContainer createChartPositionContainer() {
		return new ChartPositionContainer();
	}

	public ShowBroadcastContainer createShowBroadcastContainer() {
		return new ShowBroadcastContainer();
	}

	public SongBroadcastContainer createSongBroadcastContainer() {
		return new SongBroadcastContainer();
	}

	public SongChartPositionContainer createSongChartPositionContainer() {
		return new SongChartPositionContainer();
	}

	public SongContainer createSongContainer() {
		return new SongContainer();
	}

	public ChartService getChartService() {
		return chartService;
	}

	public ContentService getContentService() {
		return contentService;
	}

	public StationService getStationService() {
		return stationService;
	}

}
