package de.airsupply.airplay.web.ui.views;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import ru.xpoft.vaadin.VaadinView;

import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;

import de.airsupply.airplay.web.ui.components.ContentPanel;
import de.airsupply.airplay.web.ui.components.SongStatisticsComponent;
import de.airsupply.airplay.web.ui.model.Containers.SongContainer;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@VaadinView(SongView.NAME)
@SuppressWarnings("serial")
public class SongView extends ContentPanel implements View {

	public static final String NAME = "songView";

	@Autowired
	private SongContainer songContainer;

	@Autowired
	private SongStatisticsComponent songStatisticsComponent;

	@Override
	public void enter(ViewChangeEvent event) {
	}

	@Override
	protected void init() {
		final Table table = new Table("Songs");
		table.setEnabled(false);
		table.setSizeFull();
		table.setContainerDataSource(songContainer);
		table.setVisibleColumns(songContainer.getPropertyIds());
		table.setColumnHeaders(songContainer.getColumnHeaders());
		table.setSelectable(true);
		table.setImmediate(true);
		table.addValueChangeListener(songStatisticsComponent);

		final CheckBox checkBox = new CheckBox("Advanced Search", false);
		TextField searchField = new TextField("Search");
		searchField.setRequired(true);
		searchField.addTextChangeListener(new TextChangeListener() {

			@Override
			public void textChange(TextChangeEvent event) {
				table.setEnabled(songContainer.search(event.getText(), checkBox.getValue().booleanValue()));
				table.select(table.getNullSelectionItemId());
				table.sort(songContainer.getPropertyIds(), songContainer.getSortDirections());
			}

		});

		addComponent(searchField);
		addComponent(checkBox);
		addComponent(table);
		addComponent(songStatisticsComponent);
	}

}