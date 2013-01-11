package de.airsupply.airplay.web.ui.component;

import java.util.Date;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.AbstractSelect.Filtering;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.DateField;
import com.vaadin.ui.HorizontalLayout;

import de.airsupply.airplay.core.model.Chart;
import de.airsupply.airplay.web.application.model.Containers.ChartContainer;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@SuppressWarnings("serial")
public class ChartSelectorComponent extends HorizontalLayout {

	@Autowired
	private ChartContainer chartContainer;

	private ComboBox comboBox;

	private DateField dateField;

	public ChartSelectorComponent() {
		super();
		setSpacing(true);
	}

	public ComboBox getComboBox() {
		return comboBox;
	}

	public DateField getDateField() {
		return dateField;
	}

	public Chart getSelectedChart() {
		return (Chart) comboBox.getValue();
	}

	public Date getSelectedDate() {
		return (Date) dateField.getValue();
	}

	@PostConstruct
	protected void init() {
		comboBox = new ComboBox("Please select a Chart type:");
		comboBox.setContainerDataSource(chartContainer);
		comboBox.setFilteringMode(Filtering.FILTERINGMODE_OFF);
		comboBox.setItemCaptionMode(AbstractSelect.ITEM_CAPTION_MODE_PROPERTY);
		comboBox.setItemCaptionPropertyId("name");
		comboBox.setNullSelectionAllowed(false);
		comboBox.setImmediate(true);

		chartContainer.update();

		dateField = new DateField("Please select the date:");
		dateField.setValue(new Date());
		dateField.setResolution(DateField.RESOLUTION_DAY);
		dateField.setShowISOWeekNumbers(true);
		dateField.setImmediate(true);

		addComponent(comboBox);
		addComponent(dateField);
	}

}