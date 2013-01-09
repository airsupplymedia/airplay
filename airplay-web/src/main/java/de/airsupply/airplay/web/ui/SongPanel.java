package de.airsupply.airplay.web.ui;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.ui.Accordion;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;

import de.airsupply.airplay.core.model.Chart;
import de.airsupply.airplay.core.services.ChartService;
import de.airsupply.airplay.web.application.model.Containers.SongContainer;
import de.airsupply.airplay.web.ui.WorkbenchWindow.ContentPanel;

@Component
@SuppressWarnings("serial")
class SongPanel extends ContentPanel {

	@Autowired
	private transient ChartService chartService;

	@Autowired
	private SongBroadcastPanel songBroadcastPanel;

	@Autowired
	private SongContainer songContainer;

	@Override
	protected void init() {
		final String[] propertyIds = new String[] { "artist.name", "name" };
		final String[] columnHeaders = new String[] { "Artist", "Song" };
		final boolean[] sortDirections = new boolean[propertyIds.length];
		Arrays.fill(sortDirections, true);

		final Table table = new Table("Songs");
		table.setEnabled(false);
		table.setSizeFull();
		table.setContainerDataSource(songContainer);
		table.setVisibleColumns(propertyIds);
		table.setColumnHeaders(columnHeaders);
		table.setSelectable(true);
		table.setImmediate(true);

		final Accordion accordion = new Accordion();
		table.addListener(songBroadcastPanel);
		accordion.addTab(songBroadcastPanel, "Broadcasts");

		List<Chart> charts = chartService.getCharts();
		for (Chart chart : charts) {
			final SongChartPositionPanel panel = new SongChartPositionPanel();
			panel.update(chart);
			table.addListener(panel);
			accordion.addTab(panel, chart.getName());
		}
		accordion.setSizeFull();

		final CheckBox checkBox = new CheckBox("Advanced Search", false);
		final TextField searchField = new TextField("Search");
		searchField.setRequired(true);
		searchField.addListener(new TextChangeListener() {

			@Override
			public void textChange(TextChangeEvent event) {
				table.setEnabled(songContainer.search(event.getText(), checkBox.booleanValue()));
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