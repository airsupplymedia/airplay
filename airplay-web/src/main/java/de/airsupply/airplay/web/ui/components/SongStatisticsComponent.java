package de.airsupply.airplay.web.ui.components;

import java.util.Arrays;
import java.util.Iterator;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.vaadin.data.Container;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.Table;

import de.airsupply.airplay.core.model.Chart;
import de.airsupply.airplay.core.model.Song;
import de.airsupply.airplay.web.ui.model.Containers.ChartContainer;
import de.airsupply.airplay.web.ui.model.Containers.ChartPositionContainer;
import de.airsupply.airplay.web.ui.model.Containers.SongBroadcastContainer;
import de.airsupply.airplay.web.ui.model.Containers.SongContainer;
import de.airsupply.airplay.web.ui.util.WeekOfYearColumnGenerator;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@SuppressWarnings("serial")
public class SongStatisticsComponent extends TabSheet implements ValueChangeListener {

	private static abstract class AbstractSongStatisticsLayout extends ContentPanel {

		public AbstractSongStatisticsLayout() {
			super();
			setSizeFull();
			setMargin(false);
			setSpacing(false);
		}

		protected Table createTable(Container container, String[] propertyIds, String[] columnHeaders,
				boolean[] sortDirections) {
			Table table = new Table();
			table.setEnabled(false);
			table.setSizeFull();
			table.setContainerDataSource(container);
			table.setVisibleColumns(propertyIds);
			table.setColumnHeaders(columnHeaders);
			table.sort(propertyIds, sortDirections);
			return table;
		}

		protected abstract void update(Song song);

	}

	@Configurable
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	private static class ChartPositionLayout extends AbstractSongStatisticsLayout {

		private transient Chart chart;

		@Autowired
		private ChartPositionContainer chartPositionContainer;

		private Table table;

		public ChartPositionLayout(Chart chart) {
			super();
			Assert.notNull(chart);
			this.chart = chart;
		}

		@Override
		@PostConstruct
		protected void init() {
			String[] propertyIds = new String[] { "chartState.weekDate", "position" };
			String[] columnHeaders = new String[] { "Week", "Position" };
			boolean[] sortDirections = new boolean[propertyIds.length];
			Arrays.fill(sortDirections, true);

			if (table == null) {
				table = createTable(chartPositionContainer, propertyIds, columnHeaders, sortDirections);
				table.addGeneratedColumn("chartState.weekDate", new WeekOfYearColumnGenerator());
			}

			addComponent(table);
		}

		@Override
		protected void update(Song song) {
			if (song != null) {
				chartPositionContainer.update(chart, song);
				table.setEnabled(true);
				table.sort();
			} else {
				table.setEnabled(false);
				table.removeAllItems();
			}
		}

	}

	@Configurable
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	private static class SongBroadcastLayout extends AbstractSongStatisticsLayout {

		@Autowired
		private SongBroadcastContainer songBroadcastContainer;

		private Table table;

		@Override
		@PostConstruct
		protected void init() {
			String[] propertyIds = new String[] { "station.name", "fromDate", "toDate", "count" };
			String[] columnHeaders = new String[] { "Station", "From", "To", "Count" };
			boolean[] sortDirections = new boolean[propertyIds.length];
			Arrays.fill(sortDirections, true);

			if (table == null) {
				table = createTable(songBroadcastContainer, propertyIds, columnHeaders, sortDirections);
				table.addGeneratedColumn("fromDate", new WeekOfYearColumnGenerator());
				table.addGeneratedColumn("toDate", new WeekOfYearColumnGenerator());
			}

			addComponent(table);
		}

		@Override
		protected void update(Song song) {
			if (song != null) {
				songBroadcastContainer.update(song);
				table.setEnabled(true);
				table.sort();
			} else {
				table.setEnabled(false);
				table.removeAllItems();
			}
		}

	}

	@Autowired
	private transient ChartContainer chartContainer;

	@Autowired
	private SongContainer songContainer;

	public SongStatisticsComponent() {
		super();
		setSizeFull();
		setEnabled(false);
	}

	@PostConstruct
	protected void init() {
		addTab(new SongBroadcastLayout(), "Broadcasts");
		for (Chart chart : chartContainer.getCharts()) {
			addTab(new ChartPositionLayout(chart), chart.getName());
		}
	}

	public void update(Song song) {
		Iterator<com.vaadin.ui.Component> iterator = iterator();
		while (iterator.hasNext()) {
			com.vaadin.ui.Component component = iterator.next();
			if (component instanceof AbstractSongStatisticsLayout) {
				((AbstractSongStatisticsLayout) component).update(song);
			}
		}
	}

	@Override
	public void valueChange(ValueChangeEvent event) {
		Object value = event.getProperty().getValue();
		if (value instanceof Long && songContainer.hasSong((Long) value)) {
			setEnabled(true);
			update(songContainer.getSong((Long) value));
		} else {
			setEnabled(false);
		}
	}

}
