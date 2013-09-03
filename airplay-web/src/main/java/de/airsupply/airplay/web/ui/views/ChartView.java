package de.airsupply.airplay.web.ui.views;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import ru.xpoft.vaadin.VaadinView;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Table;

import de.airsupply.airplay.core.model.Chart;
import de.airsupply.airplay.web.ui.components.ChartSelectorComponent;
import de.airsupply.airplay.web.ui.components.ChartSelectorComponent.ChartSelectorListener;
import de.airsupply.airplay.web.ui.components.ContentPanel;
import de.airsupply.airplay.web.ui.model.Containers.ChartPositionContainer;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@VaadinView(ChartView.NAME)
@SuppressWarnings("serial")
public class ChartView extends ContentPanel implements View {

	public static final String NAME = "chartView";

	@Autowired
	private ChartPositionContainer chartPositionContainer;

	@Autowired
	private ChartSelectorComponent chartSelectorComponent;

	@Override
	public void enter(ViewChangeEvent event) {
	}

	@Override
	protected void init() {
		chartPositionContainer.initialize(false);

		final Table table = new Table("Chart Positions");
		table.setSizeFull();
		table.setEnabled(false);
		table.setContainerDataSource(chartPositionContainer);
		table.setVisibleColumns(chartPositionContainer.getPropertyIds());
		table.setColumnHeaders(chartPositionContainer.getColumnHeaders());

		chartSelectorComponent.addListener(new ChartSelectorListener() {

			@Override
			public void valueChange(Chart selectedChart, Date selectedDate) {
				table.setEnabled(chartPositionContainer.update(selectedChart, selectedDate));
			}

		});

		addComponent(chartSelectorComponent);
		addComponent(table);
	}
}