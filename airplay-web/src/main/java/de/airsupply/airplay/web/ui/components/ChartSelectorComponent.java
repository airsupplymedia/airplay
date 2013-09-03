package de.airsupply.airplay.web.ui.components;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.shared.ui.datefield.Resolution;
import com.vaadin.ui.AbstractSelect.ItemCaptionMode;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.DateField;
import com.vaadin.ui.HorizontalLayout;

import de.airsupply.airplay.core.model.Chart;
import de.airsupply.airplay.web.ui.model.Containers.ChartContainer;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@SuppressWarnings("serial")
public class ChartSelectorComponent extends HorizontalLayout {

	public static interface ChartSelectorListener {

		public void valueChange(Chart selectedChart, Date selectedDate);

	}

	@Autowired
	private ChartContainer chartContainer;

	private ComboBox comboBox;

	private DateField dateField;

	private List<ChartSelectorListener> listeners;

	public ChartSelectorComponent() {
		super();
		setSpacing(true);
	}

	public void addListener(ChartSelectorListener listener) {
		if (listeners == null) {
			listeners = new ArrayList<>();
		}
		if (!listeners.contains(listener)) {
			listeners.add(listener);
		}
	}

	public ComboBox getComboBox() {
		return comboBox;
	}

	public DateField getDateField() {
		return dateField;
	}

	public Chart getSelectedChart() {
		return chartContainer.getItem(comboBox.getValue()).getBean();
	}

	public Date getSelectedDate() {
		return (Date) dateField.getValue();
	}

	@PostConstruct
	protected void init() {
		comboBox = new ComboBox("Please select a Chart type:");
		comboBox.setContainerDataSource(chartContainer);
		comboBox.setFilteringMode(FilteringMode.OFF);
		comboBox.setItemCaptionMode(ItemCaptionMode.PROPERTY);
		comboBox.setItemCaptionPropertyId("name");
		comboBox.setNullSelectionAllowed(false);
		comboBox.setImmediate(true);
		comboBox.addValueChangeListener(new ValueChangeListener() {

			@Override
			public void valueChange(ValueChangeEvent event) {
				Chart selectedChart = getSelectedChart();
				dateField.setEnabled(selectedChart != null);
				notifyListeners(getSelectedChart(), getSelectedDate());
			}

		});

		chartContainer.update();

		dateField = new DateField("Please select the date:");
		dateField.setEnabled(false);
		dateField.setValue(new Date());
		dateField.setResolution(Resolution.DAY);
		dateField.setShowISOWeekNumbers(true);
		dateField.setImmediate(true);
		dateField.addValueChangeListener(new ValueChangeListener() {

			@Override
			public void valueChange(ValueChangeEvent event) {
				notifyListeners(getSelectedChart(), getSelectedDate());
			}

		});

		addComponent(comboBox);
		addComponent(dateField);
	}

	protected void notifyListeners(Chart selectedChart, Date selectedDate) {
		if (listeners == null) {
			return;
		}
		for (ChartSelectorListener listener : listeners) {
			listener.valueChange(selectedChart, selectedDate);
		}
	}

	public void removeListener(ChartSelectorListener listener) {
		if (listeners != null) {
			listeners.remove(listener);
		}
	}

}