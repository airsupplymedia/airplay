package de.airsupply.airplay.web.ui.panel;

import java.util.Arrays;
import java.util.Date;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.ui.Table;

import de.airsupply.airplay.core.model.Chart;
import de.airsupply.airplay.web.application.model.Containers.ChartPositionContainer;
import de.airsupply.airplay.web.ui.component.ChartSelectorComponent;
import de.airsupply.airplay.web.ui.panel.WorkbenchWindow.ContentPanel;

@Component
@SuppressWarnings("serial")
class ChartPanel extends ContentPanel {

	@Autowired
	private ChartPositionContainer chartPositionContainer;

	@Autowired
	private ChartSelectorComponent chartSelectorComponent;

	@Override
	protected void init() {
		final String[] propertyIds = new String[] { "position", "song.artist.name", "song.name" };
		final String[] columnHeaders = new String[] { "Position", "Artist", "Song" };
		final boolean[] sortDirections = new boolean[propertyIds.length];
		Arrays.fill(sortDirections, true);

		final Table table = new Table("Chart Positions");
		table.setSizeFull();
		table.setContainerDataSource(chartPositionContainer);
		table.setVisibleColumns(propertyIds);
		table.setColumnHeaders(columnHeaders);

		chartSelectorComponent.getComboBox().addListener(new ValueChangeListener() {

			@Override
			public void valueChange(ValueChangeEvent event) {
				Chart chart = (Chart) event.getProperty().getValue();
				table.setEnabled(chartPositionContainer.update(chart, chartSelectorComponent.getSelectedDate()));
			}
		});

		chartSelectorComponent.getDateField().addListener(new ValueChangeListener() {

			@Override
			public void valueChange(ValueChangeEvent event) {
				Date date = (Date) event.getProperty().getValue();
				table.setEnabled(chartPositionContainer.update(chartSelectorComponent.getSelectedChart(), date));
			}

		});

		addComponent(chartSelectorComponent);
		addComponent(table);
	}

}