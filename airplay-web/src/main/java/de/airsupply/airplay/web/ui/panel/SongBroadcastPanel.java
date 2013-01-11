package de.airsupply.airplay.web.ui.panel;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.ui.Table;

import de.airsupply.airplay.core.model.Song;
import de.airsupply.airplay.web.application.model.Containers.SongBroadcastContainer;
import de.airsupply.airplay.web.ui.panel.WorkbenchWindow.ContentPanel;
import de.airsupply.airplay.web.ui.util.WeekOfYearColumnGenerator;

@Component
@SuppressWarnings("serial")
public class SongBroadcastPanel extends ContentPanel implements ValueChangeListener {

	@Autowired
	private SongBroadcastContainer songBroadcastContainer;

	private Table table;

	public SongBroadcastPanel() {
		super();
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
		table.setContainerDataSource(songBroadcastContainer);
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