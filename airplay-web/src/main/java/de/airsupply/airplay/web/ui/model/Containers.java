package de.airsupply.airplay.web.ui.model;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.data.neo4j.support.Neo4jTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.vaadin.data.util.BeanContainer;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.HierarchicalContainer;

import de.airsupply.airplay.core.model.Artist;
import de.airsupply.airplay.core.model.Chart;
import de.airsupply.airplay.core.model.ChartPosition;
import de.airsupply.airplay.core.model.PersistentNode;
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

	public static abstract class AbstractPersistentNodeContainer<T extends PersistentNode> extends
			BeanContainer<Long, T> {

		private String[] columnHeaders;

		@Autowired
		private transient Neo4jTemplate neo4jTemplate;

		private String[] propertyIds;

		private boolean[] sortDirections;

		public AbstractPersistentNodeContainer(Class<? super T> type) {
			super(type);
			setBeanIdProperty(PersistentNode.ID_NAME);
		}

		protected void fetch(Neo4jTemplate neo4jTemplate, T bean) {
		}

		public String[] getColumnHeaders() {
			if (columnHeaders == null) {
				columnHeaders = new String[] {};
			}
			return columnHeaders;
		}

		@Override
		public BeanItem<T> getItem(Object itemId) {
			BeanItem<T> item = super.getItem(itemId);
			fetch(neo4jTemplate, item.getBean());
			return item;
		}

		public Object[] getPropertyIds() {
			if (propertyIds == null) {
				propertyIds = new String[] {};
			}
			return propertyIds;
		}

		public boolean[] getSortDirections() {
			if (sortDirections == null) {
				sortDirections = new boolean[getPropertyIds().length];
				Arrays.fill(sortDirections, true);
			}
			return sortDirections;
		}

		protected void setColumnHeaders(String[] columnHeaders) {
			this.columnHeaders = columnHeaders;
		}

		protected void setPropertyIds(String[] propertyIds) {
			this.propertyIds = propertyIds;
		}

		protected void setSortDirections(boolean[] sortDirections) {
			this.sortDirections = sortDirections;
		}

		public void update(Collection<T> objects) {
			removeAllItems();
			addAll(objects);
		}

	}

	@Component
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public static class ArtistContainer extends AbstractPersistentNodeContainer<Artist> {

		@Autowired
		private transient ContentService contentService;

		public ArtistContainer() {
			super(Artist.class);
		}

		public void update() {
			removeAllItems();
			addAll(contentService.getArtists());
		}

	}

	@Component
	public static class ChartContainer extends AbstractPersistentNodeContainer<Chart> {

		@Autowired
		private transient ChartService chartService;

		public ChartContainer() {
			super(Chart.class);
		}

		public List<Chart> getCharts() {
			return chartService.getCharts();
		}

		public void update() {
			addAll(chartService.getCharts());
		}

	}

	@Component
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public static class ChartPositionContainer extends AbstractPersistentNodeContainer<ChartPosition> {

		@Autowired
		private transient ChartService chartService;

		private boolean songAware = false;

		public ChartPositionContainer() {
			super(ChartPosition.class);
		}

		public void initialize(boolean songAware) {
			this.songAware = songAware;
			addNestedContainerProperty("chartState.chart.name");
			addNestedContainerProperty("chartState.weekDate");
			if (songAware) {
				setPropertyIds(new String[] { "chartState.weekDate", "position" });
				setColumnHeaders(new String[] { "Week", "Position" });
			} else {
				setPropertyIds(new String[] { "position", "song.artist.name", "song.name" });
				setColumnHeaders(new String[] { "Position", "Artist", "Song" });
				addNestedContainerProperty("song.artist.name");
				addNestedContainerProperty("song.name");
			}
		}

		@Override
		protected void fetch(Neo4jTemplate neo4jTemplate, ChartPosition bean) {
			if (songAware) {
				neo4jTemplate.fetch(bean.getChartState());
			} else {
				neo4jTemplate.fetch(bean.getSong());
				neo4jTemplate.fetch(bean.getSong().getArtist());
			}
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

	}

	@Component
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public static class PublisherContainer extends AbstractPersistentNodeContainer<Publisher> {

		@Autowired
		private transient ContentService contentService;

		public PublisherContainer() {
			super(Publisher.class);
		}

		public void update() {
			removeAllItems();
			addAll(contentService.getPublishers());
		}

	}

	@Component
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public static class RecordCompanyContainer extends AbstractPersistentNodeContainer<RecordCompany> {

		@Autowired
		private transient ContentService contentService;

		public RecordCompanyContainer() {
			super(RecordCompany.class);
		}

		public void update() {
			removeAllItems();
			addAll(contentService.getRecordCompanies());
		}

	}

	@Component
	public static class RecordImportContainer extends AbstractPersistentNodeContainer<RecordImport> {

		@Autowired
		private transient ImportService importService;

		public RecordImportContainer() {
			super(RecordImport.class);
			setPropertyIds(new String[] { "weekDate" });
			setColumnHeaders(new String[] { "Week Of Year" });
		}

		public void update() {
			addAll(importService.getRecordImports());
		}

	}

	@Component
	public static class RecordImportCategoryContainer extends HierarchicalContainer {

		@Autowired
		private transient ImportService importService;

		private transient RecordImport recordImport;

		public RecordImportCategoryContainer() {
			super();
		}

		@Override
		public Collection<?> getChildren(Object itemId) {
			return importService.getImportedRecordsToRevert(recordImport, itemId.toString());
		}

		public void update(RecordImport recordImport) {
			removeAllItems();
			this.recordImport = recordImport;
			for (String category : recordImport.getCategories()) {
				addItem(category);
			}
		}

	}

	@Component
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public static class ShowBroadcastContainer extends AbstractPersistentNodeContainer<ShowBroadcast> {

		@Autowired
		private transient StationService stationService;

		public ShowBroadcastContainer() {
			super(ShowBroadcast.class);
			addNestedContainerProperty("station.name");
			addNestedContainerProperty("station.longName");
		}

		@Override
		protected void fetch(Neo4jTemplate neo4jTemplate, ShowBroadcast bean) {
			neo4jTemplate.fetch(bean.getStation());
		}

		public void update(Show show) {
			removeAllItems();
			addAll(stationService.findBroadcasts(show));
		}

	}

	@Component
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public static class SongBroadcastContainer extends AbstractPersistentNodeContainer<SongBroadcast> {

		@Autowired
		private transient StationService stationService;

		public SongBroadcastContainer() {
			super(SongBroadcast.class);
			setPropertyIds(new String[] { "station.name", "fromDate", "toDate", "count" });
			setColumnHeaders(new String[] { "Station", "From", "To", "Count" });
			addNestedContainerProperty("station.name");
			addNestedContainerProperty("station.longName");
		}

		@Override
		protected void fetch(Neo4jTemplate neo4jTemplate, SongBroadcast bean) {
			neo4jTemplate.fetch(bean.getStation());
		}

		public void update(Song song) {
			removeAllItems();
			addAll(stationService.findBroadcasts(song));
		}

	}

	@Component
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public static class SongContainer extends AbstractPersistentNodeContainer<Song> {

		@Autowired
		private transient ContentService contentService;

		public SongContainer() {
			super(Song.class);
			setPropertyIds(new String[] { "artist.name", "name" });
			setColumnHeaders(new String[] { "Artist", "Song" });
			addNestedContainerProperty("artist.name");
		}

		@Override
		protected void fetch(Neo4jTemplate neo4jTemplate, Song bean) {
			neo4jTemplate.fetch(bean.getArtist());
		}

		public Song getSong(Long id) {
			return contentService.getSong(id);
		}

		public boolean hasSong(Long id) {
			return contentService.hasSong(id);
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

	}

	private Containers() {
		super();
	}

}
