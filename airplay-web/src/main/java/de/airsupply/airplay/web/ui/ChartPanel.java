package de.airsupply.airplay.web.ui;

import java.util.Arrays;
import java.util.Date;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.AbstractSelect.Filtering;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.DateField;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Table;

import de.airsupply.airplay.core.model.Chart;
import de.airsupply.airplay.web.application.model.AirplayDataProvider;
import de.airsupply.airplay.web.application.model.AirplayDataProvider.ChartPositionContainer;
import de.airsupply.airplay.web.ui.WorkbenchWindow.ContentPanel;

@SuppressWarnings("serial")
class ChartPanel extends ContentPanel {

	public ChartPanel(AirplayDataProvider dataProvider) {
		super(dataProvider);
	}

	@Override
	protected void init() {
		final HorizontalLayout horizontalLayout = new HorizontalLayout();
		horizontalLayout.setSpacing(true);

		final ComboBox comboBox = new ComboBox("Please select a Chart type");
		comboBox.setContainerDataSource(getDataProvider().createChartContainer());
		comboBox.setFilteringMode(Filtering.FILTERINGMODE_OFF);
		comboBox.setItemCaptionMode(AbstractSelect.ITEM_CAPTION_MODE_PROPERTY);
		comboBox.setItemCaptionPropertyId("name");
		comboBox.setNullSelectionAllowed(false);
		comboBox.setImmediate(true);

		final DateField dateField = new DateField("Please select the date:");
		dateField.setValue(new java.util.Date());
		dateField.setResolution(DateField.RESOLUTION_DAY);
		dateField.setImmediate(true);

		final ChartPositionContainer container = getDataProvider().createChartPositionContainer();

		final String[] propertyIds = new String[] { "position", "song.artist.name", "song.name" };
		final String[] columnHeaders = new String[] { "Position", "Artist", "Song" };
		final boolean[] sortDirections = new boolean[propertyIds.length];
		Arrays.fill(sortDirections, true);

		final Table table = new Table("Chart Positions");
		table.setSizeFull();
		table.setContainerDataSource(container);
		table.setVisibleColumns(propertyIds);
		table.setColumnHeaders(columnHeaders);

		comboBox.addListener(new ValueChangeListener() {

			@Override
			public void valueChange(ValueChangeEvent event) {
				Chart chart = (Chart) event.getProperty().getValue();
				table.setEnabled(container.update(chart, (Date) dateField.getValue()));
			}
		});

		dateField.addListener(new ValueChangeListener() {

			@Override
			public void valueChange(ValueChangeEvent event) {
				Date date = (Date) event.getProperty().getValue();
				table.setEnabled(container.update((Chart) comboBox.getValue(), date));
			}

		});

		horizontalLayout.addComponent(comboBox);
		horizontalLayout.addComponent(dateField);
		addComponent(horizontalLayout);
		addComponent(table);
	}
}