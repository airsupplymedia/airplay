package de.airsupply.airplay.web.ui;

import java.util.Arrays;
import java.util.List;

import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.ui.Accordion;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;

import de.airsupply.airplay.core.model.Chart;
import de.airsupply.airplay.web.application.model.AirplayDataProvider;
import de.airsupply.airplay.web.application.model.AirplayDataProvider.SongContainer;
import de.airsupply.airplay.web.ui.WorkbenchWindow.ContentPanel;

@SuppressWarnings("serial")
class SongPanel extends ContentPanel {

	public SongPanel(AirplayDataProvider dataProvider) {
		super(dataProvider);
	}

	@Override
	protected void init() {
		final SongContainer container = getDataProvider().createSongContainer();

		final String[] propertyIds = new String[] { "artist.name", "name" };
		final String[] columnHeaders = new String[] { "Artist", "Song" };
		final boolean[] sortDirections = new boolean[propertyIds.length];
		Arrays.fill(sortDirections, true);

		final Table table = new Table("Songs");
		table.setEnabled(false);
		table.setSizeFull();
		table.setContainerDataSource(container);
		table.setVisibleColumns(propertyIds);
		table.setColumnHeaders(columnHeaders);
		table.setSelectable(true);
		table.setImmediate(true);

		final Accordion accordion = new Accordion();
		final SongBroadcastPanel songBroadcastPanel = new SongBroadcastPanel(getDataProvider());
		table.addListener(songBroadcastPanel);
		accordion.addTab(songBroadcastPanel, "Broadcasts");

		List<Chart> charts = getDataProvider().getChartService().getCharts();
		for (Chart chart : charts) {
			final SongChartPositionPanel songChartPositionPanel = new SongChartPositionPanel(chart, getDataProvider());
			table.addListener(songChartPositionPanel);
			accordion.addTab(songChartPositionPanel, chart.getName());
		}
		accordion.setSizeFull();

		final CheckBox checkBox = new CheckBox("Advanced Search", false);
		final TextField searchField = new TextField("Search");
		searchField.setRequired(true);
		searchField.addListener(new TextChangeListener() {

			@Override
			public void textChange(TextChangeEvent event) {
				table.setEnabled(container.search(event.getText(), checkBox.booleanValue()));
				table.select(table.getNullSelectionItemId());
				table.sort(propertyIds, sortDirections);
			}

		});

		addComponent(searchField);
		addComponent(checkBox);
		addComponent(table);
		addComponent(accordion);
	}

}