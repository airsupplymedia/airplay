package de.airsupply.airplay.web.ui.util;

import java.util.Date;

import com.vaadin.data.Property;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.ColumnGenerator;

import de.airsupply.commons.core.util.DateUtils;

@SuppressWarnings("serial")
public final class WeekOfYearColumnGenerator implements ColumnGenerator {

	@Override
	public Object generateCell(Table source, Object itemId, Object columnId) {
		Property<?> property = source.getItem(itemId).getItemProperty(columnId);
		if (property != null && property.getValue() instanceof Date) {
			return DateUtils.getWeekOfYearFormat((Date) property.getValue());
		}
		return null;
	}

}