package de.airsupply.airplay.web.ui.views;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.ui.Table;

import de.airsupply.airplay.core.model.Chart;
import de.airsupply.airplay.core.model.Song;
import de.airsupply.airplay.web.ui.components.ContentPanel;
import de.airsupply.airplay.web.ui.model.Containers.ChartPositionContainer;
import de.airsupply.airplay.web.ui.util.WeekOfYearColumnGenerator;

@Configurable
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@SuppressWarnings("serial")
public class SongChartPositionPanel extends ContentPanel implements ValueChangeListener {

	private transient Chart chart;

	@Autowired
	private ChartPositionContainer chartPositionContainer;

	private Table table;

	public SongChartPositionPanel() {
		super();
		setSizeFull();
		setMargin(false);
		setSpacing(false);
	}

	@Override
	protected void init() {
		final String[] propertyIds = new String[] { "chartState.weekDate", "position" };
		final String[] columnHeaders = new String[] { "Week", "Position" };
		final boolean[] sortDirections = new boolean[propertyIds.length];
		Arrays.fill(sortDirections, true);

		table = new Table();
		table.setEnabled(false);
		table.setSizeFull();
		table.setContainerDataSource(chartPositionContainer);
		table.setVisibleColumns(propertyIds);
		table.setColumnHeaders(columnHeaders);
		table.sort(propertyIds, sortDirections);
		table.addGeneratedColumn("chartState.weekDate", new WeekOfYearColumnGenerator());

		addComponent(table);
	}

	public void update(Chart chart) {
		this.chart = chart;
	}

	@Override
	public void valueChange(ValueChangeEvent event) {
		if (event.getProperty().getValue() instanceof Song) {
			Song song = (Song) event.getProperty().getValue();
			chartPositionContainer.update(chart, song);
			table.setEnabled(true);
			table.sort();
		} else {
			table.setEnabled(false);
			table.removeAllItems();
		}
	}

}