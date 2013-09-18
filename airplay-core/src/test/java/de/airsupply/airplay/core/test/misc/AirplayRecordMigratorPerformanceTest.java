package de.airsupply.airplay.core.test.misc;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import de.airsupply.airplay.core.model.Chart;
import de.airsupply.airplay.core.model.Song;
import de.airsupply.airplay.core.model.SongBroadcast;
import de.airsupply.airplay.core.services.ChartService;
import de.airsupply.airplay.core.services.ContentService;
import de.airsupply.airplay.core.services.StationService;
import de.airsupply.commons.core.context.Loggable;
import de.airsupply.commons.core.util.CollectionUtils.Procedure;

@Component
public class AirplayRecordMigratorPerformanceTest {

	@Loggable
	private static Logger logger;

	public static void main(String[] args) {
		BatchRunner.run(new Procedure<ApplicationContext>() {

			@Override
			public void run(ApplicationContext applicationContext) {
				try {
					applicationContext.getBean(AirplayRecordMigratorPerformanceTest.class).benchmark();
				} catch (Exception exception) {
					logger.error(exception.getMessage(), exception);
				}
			}

		});
	}

	@Autowired
	private ChartService chartService;

	@Autowired
	private ContentService contentService;

	@Autowired
	private StationService stationService;

	private void benchmark() {
		Chart chart = chartService.getCharts().get(1);

		logger.info(chart.toString());

		StopWatch stopWatch = new StopWatch();

		stopWatch.start("Find Song");
		Song song = contentService.findSongs("JUST A LITTLE WHILE", false).get(0);
		logger.info(song.toString());
		stopWatch.stop();

		stopWatch.start("Find Chart Positions");
		chartService.findChartPositions(chart, song);
		stopWatch.stop();

		stopWatch.start("Find Song Broadcasts");
		stationService.findBroadcasts(song);
		stopWatch.stop();

		stopWatch.start("Find Song Broadcasts and fetch Stations");
		for (SongBroadcast broadcast : stationService.findBroadcasts(song)) {
			stationService.fetch(broadcast.getStation());
			stationService.fetch(broadcast.getBroadcastedSong());
			stationService.fetch(broadcast.getBroadcastedSong().getArtist());
		}
		stopWatch.stop();

		logger.info(stopWatch.prettyPrint());
	}
}
