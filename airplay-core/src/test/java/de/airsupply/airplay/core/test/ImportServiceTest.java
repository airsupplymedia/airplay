package de.airsupply.airplay.core.test;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StopWatch;

import de.airsupply.airplay.core.config.ApplicationConfiguration;
import de.airsupply.airplay.core.model.Artist;
import de.airsupply.airplay.core.model.Chart;
import de.airsupply.airplay.core.model.Publisher;
import de.airsupply.airplay.core.model.RecordCompany;
import de.airsupply.airplay.core.model.RecordImport;
import de.airsupply.airplay.core.model.Song;
import de.airsupply.airplay.core.model.util.LoggingRecordImportProgressProvider;
import de.airsupply.airplay.core.services.ChartService;
import de.airsupply.airplay.core.services.ContentService;
import de.airsupply.airplay.core.services.ImportService;
import de.airsupply.airplay.core.services.StationService;
import de.airsupply.airplay.core.test.config.TestConfiguration;
import de.airsupply.commons.core.context.Loggable;
import de.airsupply.commons.core.util.DateUtils;

@RunWith(SpringJUnit4ClassRunner.class)
@ActiveProfiles("test")
@ContextConfiguration(classes = { ApplicationConfiguration.class, TestConfiguration.class })
@Transactional
public class ImportServiceTest {

	@Autowired
	private ChartService chartService;

	@Autowired
	private ContentService contentService;

	@Autowired
	private ImportService importService;

	@Loggable
	private Logger logger;

	@Autowired
	private LoggingRecordImportProgressProvider progressProvider;

	@Autowired
	private StationService stationService;

	@Test
	public void testInitialImport() {
		Chart chart = chartService.save(new Chart("Airplay Charts"));
		Date week = DateUtils.getStartOfWeek(new Date());
		URL url = getClass().getResource("/COMMON_AIRPLAY_SET.sdf");
		logger.info("Using file: " + url);
		try (InputStream inputStream = url.openStream()) {
			StopWatch stopWatch = new StopWatch();
			stopWatch.start();
			importService.importRecords(chart, week, inputStream, progressProvider);
			stopWatch.stop();
			logger.info("Import took: " + stopWatch.prettyPrint());
		} catch (IOException exception) {
			logger.error(exception.getMessage(), exception);
		}
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

		RecordImport recordImport = importService.getRecordImports().get(0);
		assertEquals(1, recordImport.getImportedChartStateList().size());
		assertEquals(300, recordImport.getImportedChartPositionList().size());
		assertEquals(93, recordImport.getImportedStationList().size());
		assertEquals(6216, recordImport.getImportedSongBroadcastList().size());
		assertEquals(0, recordImport.getImportedShowBroadcastList().size());
		assertEquals(300, recordImport.getImportedSongList().size());
		assertEquals(254, recordImport.getImportedArtistList().size());
		assertEquals(24, recordImport.getImportedRecordCompanyList().size());
		assertEquals(82, recordImport.getImportedPublisherList().size());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testRepeatedImport() {
		Chart chart = chartService.save(new Chart("Airplay Charts"));
		Date week = DateUtils.getStartOfWeek(new Date());
		URL url = getClass().getResource("/INTEGRITY_CHECK_AIRPLAY_SET.sdf");
		logger.info("Using file: " + url);
		try (InputStream inputStream = url.openStream()) {
			importService.importRecords(chart, week, inputStream, progressProvider);
			importService.importRecords(chart, week, inputStream, progressProvider);
		} catch (IOException exception) {
			logger.error(exception.getMessage(), exception);
		}
	}

	@Test
	public void testRevertedImport() {
		StopWatch stopWatch = new StopWatch();
		Chart chart = chartService.save(new Chart("Airplay Charts"));
		Date week = DateUtils.getStartOfWeek(new Date());
		URL url = getClass().getResource("/COMMON_AIRPLAY_SET.sdf");
		logger.info("Using file: " + url);
		try (InputStream inputStream = url.openStream()) {
			stopWatch.start();
			importService.importRecords(chart, week, inputStream, progressProvider);
			stopWatch.stop();
			logger.info("Import took: " + stopWatch.prettyPrint());
		} catch (IOException exception) {
			logger.error(exception.getMessage(), exception);
		}

		RecordImport recordImport = importService.getRecordImports().get(0);
		assertEquals(1, chartService.getChartCount());
		assertEquals(1, importService.getImportedRecordsToRevert(recordImport, "IMPORTED_CHART_STATES").size());
		assertEquals(300, importService.getImportedRecordsToRevert(recordImport, "IMPORTED_CHART_POSITIONS").size());
		assertEquals(0, importService.getImportedRecordsToRevert(recordImport, "IMPORTED_SHOWS").size());
		assertEquals(93, importService.getImportedRecordsToRevert(recordImport, "IMPORTED_STATIONS").size());
		assertEquals(6216, importService.getImportedRecordsToRevert(recordImport, "IMPORTED_SONG_BROADCASTS").size());
		assertEquals(0, importService.getImportedRecordsToRevert(recordImport, "IMPORTED_SHOW_BROADCASTS").size());
		assertEquals(300, importService.getImportedRecordsToRevert(recordImport, "IMPORTED_SONGS").size());
		assertEquals(254, importService.getImportedRecordsToRevert(recordImport, "IMPORTED_ARTISTS").size());
		assertEquals(24, importService.getImportedRecordsToRevert(recordImport, "IMPORTED_RECORD_COMPANIES").size());
		assertEquals(82, importService.getImportedRecordsToRevert(recordImport, "IMPORTED_PUBLISHERS").size());
		assertEquals(7270, importService.getImportedRecordsToRevert(recordImport).size());

		stopWatch.start();
		importService.revertImport(recordImport);
		stopWatch.stop();
		logger.info("Reverting took: " + stopWatch.prettyPrint());

		assertEquals(1, chartService.getChartCount());
		assertEquals(0, chartService.getChartStateCount());
		assertEquals(0, chartService.getChartPositionCount());
		assertEquals(0, stationService.getShowCount());
		assertEquals(0, stationService.getStationCount());
		assertEquals(0, stationService.getSongBroadcastCount());
		assertEquals(0, stationService.getShowBroadcastCount());
		assertEquals(0, contentService.getSongCount());
		assertEquals(0, contentService.getArtistCount());
		assertEquals(0, contentService.getRecordCompanyCount());
		assertEquals(0, contentService.getPublisherCount());
		assertEquals(0, importService.getRecordImportCount());
	}

	@Test
	public void testRevertedImportWithDependencies() {
		Chart chart = chartService.save(new Chart("Airplay Charts"));
		Date week = DateUtils.getStartOfWeek(new Date());
		URL url = getClass().getResource("/INTEGRITY_CHECK_AIRPLAY_SET.sdf");
		logger.info("Using file: " + url);
		try (InputStream inputStream = url.openStream()) {
			importService.importRecords(chart, week, inputStream, progressProvider);
		} catch (IOException exception) {
			logger.error(exception.getMessage(), exception);
		}

		Artist importedArtist = contentService.getArtists().get(0);
		RecordCompany importedRecordCompany = contentService.getRecordCompanies().get(0);
		Publisher importedPublisher = contentService.getPublishers().get(0);

		Song song = new Song(importedArtist, "Some Song", null, importedRecordCompany, importedPublisher);
		contentService.save(song);

		importService.revertImport(importService.getRecordImports().get(0));
		assertEquals(1, chartService.getChartCount());
		assertEquals(0, chartService.getChartStateCount());
		assertEquals(0, chartService.getChartPositionCount());
		assertEquals(0, stationService.getShowCount());
		assertEquals(0, stationService.getStationCount());
		assertEquals(0, stationService.getSongBroadcastCount());
		assertEquals(0, stationService.getShowBroadcastCount());
		assertEquals(1, contentService.getSongCount());
		assertEquals(1, contentService.getArtistCount());
		assertEquals(1, contentService.getRecordCompanyCount());
		assertEquals(1, contentService.getPublisherCount());
		assertEquals(0, importService.getRecordImportCount());
	}

}