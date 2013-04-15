package de.airsupply.airplay.web.ui;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import ru.xpoft.vaadin.DiscoveryNavigator;

import com.vaadin.annotations.Theme;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.Command;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.Panel;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Reindeer;

import de.airsupply.airplay.web.ui.views.ChartView;
import de.airsupply.airplay.web.ui.views.MainView;
import de.airsupply.airplay.web.ui.views.RecordImportView;
import de.airsupply.airplay.web.ui.views.SongView;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Theme(Reindeer.THEME_NAME)
@SuppressWarnings("serial")
public class AirplayUI extends UI {

	private MenuBar createMenu() {
		MenuBar menu = new MenuBar();
		menu.setWidth("100%");
		menu.setHeight("20%");

		createMenuItem(menu, "Main", MainView.NAME);
		createMenuItem(menu, "Songs", SongView.NAME);
		createMenuItem(menu, "Charts", ChartView.NAME);
		createMenuItem(menu, "Import Records", RecordImportView.NAME);

		return menu;
	}

	private void createMenuItem(MenuBar menuBar, String caption, final String name) {
		menuBar.addItem(caption, new Command() {

			@Override
			public void menuSelected(MenuItem selectedItem) {
				getNavigator().navigateTo(name);
			}

		});
	}

	private Panel createPanel() {
		Panel panel = new Panel();
		panel.setSizeFull();
		return panel;
	}

	@Override
	protected void init(VaadinRequest request) {
		MenuBar menu = createMenu();
		Panel panel = createPanel();

		VerticalLayout verticalLayout = new VerticalLayout();
		verticalLayout.addComponent(menu);
		verticalLayout.addComponent(panel);

		new DiscoveryNavigator(this, panel);

		getPage().setTitle("Airplay Manager");
		setSizeFull();
		setContent(verticalLayout);
	}

}
