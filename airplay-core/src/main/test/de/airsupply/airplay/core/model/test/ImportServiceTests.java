package de.airsupply.airplay.core.model.test;

import static org.junit.Assert.assertEquals;

import java.io.InputStream;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StopWatch;

import de.airsupply.airplay.core.model.Chart;
import de.airsupply.airplay.core.services.ChartService;
import de.airsupply.airplay.core.services.ContentService;
import de.airsupply.airplay.core.services.ImportService;
import de.airsupply.airplay.core.services.StationService;
import de.airsupply.commons.core.util.DateUtils;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "applicationContext-simpleTest.xml" })
@Transactional
public class ImportServiceTests {

	@Autowired
	private ChartService chartService;

	@Autowired
	private ContentService contentService;

	@Autowired
	private ImportService importService;

	private final Log log = LogFactory.getLog(getClass());

	@Autowired
	private StationService stationService;

	@Test
	public void testInitialImport() {
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();

		Chart chart = chartService.save(new Chart("Airplay Charts"));

		Date week = DateUtils.getStartOfWeek(new Date());
		InputStream inputStream = getClass().getResourceAsStream("COMMON_AIRPLAY_SET.SDF");

		importService.importRecords(chart, week, inputStream);

		assertEquals(1, chartService.getChartCount());
		assertEquals(1, chartService.getChartStateCount());
		assertEquals(300, chartService.getChartPositionCount());
		assertEquals(0, stationService.getShowCount());
		assertEquals(93, stationService.getStationCount());
		assertEquals(6216, stationService.getSongBroadcastCount());
		assertEquals(0, stationService.getShowBroadcastCount());
		assertEquals(300, contentService.getSongCount());
		assertEquals(254, contentService.getArtistCount());
		assertEquals(24, contentService.getRecordCompanyCount());
		assertEquals(82, contentService.getPublisherCount());
		assertEquals(1, importService.getRecordImportCount());

		stopWatch.stop();
		log.info(stopWatch.prettyPrint());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testRepeatedImport() {
		Chart chart = chartService.save(new Chart("Airplay Charts"));
		Date week = DateUtils.getStartOfWeek(new Date());
		InputStream inputStream = getClass().getResourceAsStream("INTEGRITY_CHECK_AIRPLAY_SET.SDF");

		importService.importRecords(chart, week, inputStream);
		importService.importRecords(chart, week, inputStream);
	}

}
