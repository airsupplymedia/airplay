package de.airsupply.airplay.web.application;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;

import com.vaadin.Application;
import com.vaadin.ui.themes.Reindeer;

import de.airsupply.airplay.web.ui.panel.WorkbenchWindow;

@Scope(WebApplicationContext.SCOPE_SESSION)
@Component
@SuppressWarnings("serial")
public class AirplayApplication extends Application {

	@Autowired
	private WorkbenchWindow window;

	@Override
	public void init() {
		setTheme(Reindeer.THEME_NAME);
		setMainWindow(window);
	}

}
