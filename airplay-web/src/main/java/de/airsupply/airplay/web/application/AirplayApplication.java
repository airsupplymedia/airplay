package de.airsupply.airplay.web.application;

import java.util.concurrent.Executors;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;

import com.vaadin.Application;
import com.vaadin.ui.themes.Reindeer;

import de.airsupply.airplay.core.importers.dbf.AirplayLegacyMigrator;
import de.airsupply.airplay.core.services.ChartService;
import de.airsupply.airplay.web.ui.panel.WorkbenchWindow;

@Scope(WebApplicationContext.SCOPE_SESSION)
@Component
@SuppressWarnings("serial")
public class AirplayApplication extends Application {

	@Autowired
	private transient ChartService chartService;

	@Autowired
	private WorkbenchWindow window;

	@Autowired
	private transient AirplayLegacyMigrator migrator;

	@PostConstruct
	public void bootstrap() {
		// FIXME REMOVE!
		chartService.createInitialData();

		// // FIXME REMOVE!
		Executors.newSingleThreadExecutor().submit(new Runnable() {

			@Override
			public void run() {
				migrator.migrateStations();
				migrator.migrateSongs();
				migrator.migrateChartStates();
				migrator.migrateChartPositions();
				migrator.migrateSongBroadcasts();
			}
		});
	}

	@Override
	public void close() {
		super.close();
	}

	@Override
	public void init() {
		setTheme(Reindeer.THEME_NAME);
		setMainWindow(window);
	}

}
