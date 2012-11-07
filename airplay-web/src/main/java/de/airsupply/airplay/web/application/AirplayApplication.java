package de.airsupply.airplay.web.application;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.vaadin.Application;
import com.vaadin.ui.themes.Reindeer;

import de.airsupply.airplay.core.importers.dbf.AirplayLegacyMigrator;
import de.airsupply.airplay.web.application.model.AirplayDataProvider;
import de.airsupply.airplay.web.ui.WorkbenchWindow;

@Component
@SuppressWarnings("serial")
public class AirplayApplication extends Application {

	@Autowired
	private transient AirplayDataProvider dataProvider;

	@Autowired
	private transient AirplayLegacyMigrator migrator;

	@Override
	public void close() {
		super.close();
	}

	@Override
	public void init() {
		Assert.notNull(dataProvider);
		setTheme(Reindeer.THEME_NAME);
		WorkbenchWindow window = new WorkbenchWindow(dataProvider);
		setMainWindow(window);

		// // FIXME REMOVE!
		// Executors.newSingleThreadExecutor().submit(new Runnable() {
		//
		// @Override
		// public void run() {
		// migrator.migrateStations();
		// migrator.migrateSongs();
		// migrator.migrateChartStates();
		// migrator.migrateChartPositions();
		// // migrator.migrateSongBroadcasts();
		// }
		// });
	}

}
