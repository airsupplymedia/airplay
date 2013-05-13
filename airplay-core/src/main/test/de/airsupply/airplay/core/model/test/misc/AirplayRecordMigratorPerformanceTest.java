package de.airsupply.airplay.core.model.test.misc;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import de.airsupply.airplay.core.model.Chart;
import de.airsupply.airplay.core.model.Song;
import de.airsupply.airplay.core.services.ChartService;
import de.airsupply.airplay.core.services.ContentService;
import de.airsupply.airplay.core.services.StationService;
import de.airsupply.commons.core.context.Loggable;

@Component
public class AirplayRecordMigratorPerformanceTest {

	@Loggable
	private static Logger logger;

	public static void main(String[] args) {
		ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext(
				"classpath*:de/airsupply/airplay/core/model/test/misc/applicationContext-batch.xml");
		applicationContext.start();
		try {
			applicationContext.getBean(AirplayRecordMigratorPerformanceTest.class).benchmark();
		} catch (Exception exception) {
			logger.error(exception.getMessage(), exception);
		}
		applicationContext.stop();
		applicationContext.close();
	}

	@Autowired
	private ChartService chartService;

	@Autowired
	private ContentService contentService;

	@Autowired
	private StationService stationService;

	private void benchmark() {
		Chart chart = chartService.getCharts().get(1);
		Song song = contentService.findSongs("JACKSON, MICHAEL", false).get(0);

		logger.info(chart.toString());
		logger.info(song.toString());

		StopWatch stopWatch = new StopWatch();
		stopWatch.start("Find Chart Positions");
		for (int i = 0; i <= 100; i++) {
			chartService.findChartPositions(chart, song);
		}
		stopWatch.stop();
		stopWatch.start("Find Song Broadcasts");
		for (int i = 0; i <= 100; i++) {
			stationService.findBroadcasts(song);
		}
		stopWatch.stop();

		logger.info(stopWatch.prettyPrint());
	}

}
