package de.airsupply.airplay.web.ui;

import java.util.Arrays;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.ui.Table;

import de.airsupply.airplay.core.model.Song;
import de.airsupply.airplay.web.application.model.AirplayDataProvider;
import de.airsupply.airplay.web.application.model.AirplayDataProvider.SongBroadcastContainer;
import de.airsupply.airplay.web.ui.WorkbenchWindow.ContentPanel;
import de.airsupply.commons.web.ui.WeekOfYearColumnGenerator;

@SuppressWarnings("serial")
public class SongBroadcastPanel extends ContentPanel implements ValueChangeListener {

	private Table table;

	public SongBroadcastPanel(AirplayDataProvider dataProvider) {
		super(dataProvider);
		setSizeFull();
		setMargin(false);
		setSpacing(false);
	}

	@Override
	protected void init() {
		final String[] propertyIds = new String[] { "station.name", "fromDate", "toDate", "count" };
		final String[] columnHeaders = new String[] { "Station", "From", "To", "Count" };
		final boolean[] sortDirections = new boolean[propertyIds.length];
		Arrays.fill(sortDirections, true);

		table = new Table();
		table.setEnabled(false);
		table.setSizeFull();
		table.setContainerDataSource(getDataProvider().createSongBroadcastContainer());
		table.setVisibleColumns(propertyIds);
		table.setColumnHeaders(columnHeaders);
		table.sort(propertyIds, sortDirections);
		table.addGeneratedColumn("fromDate", new WeekOfYearColumnGenerator());
		table.addGeneratedColumn("toDate", new WeekOfYearColumnGenerator());

		addComponent(table);
	}

	@Override
	public void valueChange(ValueChangeEvent event) {
		if (event.getProperty().getValue() instanceof Song) {
			Song song = (Song) event.getProperty().getValue();
			SongBroadcastContainer dataSource = (SongBroadcastContainer) table.getContainerDataSource();
			dataSource.update(song);
			table.setEnabled(true);
			table.sort();
		} else {
			table.setEnabled(false);
			table.removeAllItems();
		}
	}

}