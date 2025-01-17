package de.airsupply.airplay.core.test;

import static de.airsupply.commons.core.util.DateUtils.getStartOfWeek;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.validation.ConstraintViolationException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import de.airsupply.airplay.core.config.ApplicationConfiguration;
import de.airsupply.airplay.core.model.Artist;
import de.airsupply.airplay.core.model.Chart;
import de.airsupply.airplay.core.model.ChartPosition;
import de.airsupply.airplay.core.model.ChartState;
import de.airsupply.airplay.core.model.Song;
import de.airsupply.airplay.core.services.ChartService;
import de.airsupply.airplay.core.test.config.TestConfiguration;

@RunWith(SpringJUnit4ClassRunner.class)
@ActiveProfiles("test")
@ContextConfiguration(classes = { ApplicationConfiguration.class, TestConfiguration.class })
@Transactional
public class ChartServiceTest {

	@Autowired
	private ChartService service;

	@Test
	public void testChartCreation() {
		service.save(new Chart("Airplay Charts"));
		assertTrue(service.exists(new Chart("Airplay Charts")));
	}

	@Test
	public void testChartPositionCreation() {
		Chart chart = service.save(new Chart("Airplay Charts"));
		ChartState chartState = service.save(new ChartState(chart, getStartOfWeek(new Date())));
		Artist artist = service.save(new Artist("JACKSON, MICHAEL"));
		Song song = service.save(new Song(artist, "THRILLER"));
		service.save(new ChartPosition(chartState, song, 1));
		assertEquals(1, service.getCharts().get(0).getChartStateList().size());
		assertEquals(1, service.getChartStates().get(0).getChartPositionList().size());
	}

	@Test
	public void testChartPositionRetrievalByChartAndDate() {
		Date week = getStartOfWeek(new GregorianCalendar(2000, 12, 24).getTime());

		Chart chart = service.save(new Chart("Airplay Charts"));
		ChartState chartState = service.save(new ChartState(chart, week));
		service.save(new ChartState(chart, getStartOfWeek(new GregorianCalendar(2001, 12, 24).getTime())));

		Artist artist;
		Song song;

		artist = service.save(new Artist("JACKSON, MICHAEL"));
		song = service.save(new Song(artist, "THRILLER"));
		service.save(new ChartPosition(chartState, song, 1));

		artist = service.save(new Artist("PET SHOP BOYS, THE"));
		song = service.save(new Song(artist, "IT COULDN'T HAPPEN HERE"));
		service.save(new ChartPosition(chartState, song, 2));

		List<ChartPosition> chartPositions = service.findChartPositions(chart, week);
		assertEquals(2, chartPositions.size());
		assertEquals(song, chartPositions.get(1).getSong());
		assertEquals(chartState, chartPositions.get(0).getChartState());
	}

	@Test
	public void testChartPositionRetrievalByChartAndSong() {
		Date week = getStartOfWeek(new GregorianCalendar(2000, 12, 24).getTime());

		Chart chart = service.save(new Chart("Airplay Charts"));
		ChartState chartState = service.save(new ChartState(chart, week));

		Artist artist;
		Song song;

		artist = service.save(new Artist("JACKSON, MICHAEL"));
		song = service.save(new Song(artist, "THRILLER"));
		service.save(new ChartPosition(chartState, song, 1));

		artist = service.save(new Artist("PET SHOP BOYS, THE"));
		song = service.save(new Song(artist, "IT COULDN'T HAPPEN HERE"));
		service.save(new ChartPosition(chartState, song, 2));

		List<ChartPosition> chartPositions = service.findChartPositions(chart, song);
		assertEquals(1, chartPositions.size());
		assertEquals(song, chartPositions.get(0).getSong());
		assertEquals(chartState, chartPositions.get(0).getChartState());
	}

	@Test
	public void testChartPositionRetrievalLatestByChart() {
		Chart chart = service.save(new Chart("Airplay Charts"));
		ChartState oldChartState = service
				.save(new ChartState(chart, getStartOfWeek(new GregorianCalendar(2000, 10, 24).getTime())));
		ChartState newChartState = service
				.save(new ChartState(chart, getStartOfWeek(new GregorianCalendar(2000, 12, 24).getTime())));

		Artist artist;
		Song song;

		artist = service.save(new Artist("JACKSON, MICHAEL"));
		song = service.save(new Song(artist, "THRILLER"));
		service.save(new ChartPosition(oldChartState, song, 1));
		service.save(new ChartPosition(newChartState, song, 2));

		artist = service.save(new Artist("PET SHOP BOYS, THE"));
		song = service.save(new Song(artist, "IT COULDN'T HAPPEN HERE"));
		service.save(new ChartPosition(oldChartState, song, 2));
		service.save(new ChartPosition(newChartState, song, 1));

		List<ChartPosition> chartPositions = service.findLatestChartPositions(chart);
		assertEquals(2, chartPositions.size());
		assertEquals(song, chartPositions.get(0).getSong());
		assertEquals(newChartState, chartPositions.get(0).getChartState());
	}

	public void testChartStateCount() {
		Chart chart = service.save(new Chart("Airplay Charts"));
		service.save(new ChartState(chart, getStartOfWeek(new GregorianCalendar(2000, 12, 24).getTime())));
		service.save(new ChartState(chart, getStartOfWeek(new GregorianCalendar(2000, 12, 17).getTime())));
		assertEquals(2, service.getChartStateCount());
	}

	@Test
	public void testChartStateCreation() {
		Chart chart = service.save(new Chart("Airplay Charts"));
		service.save(new ChartState(chart, getStartOfWeek(new Date())));
		assertEquals(1, service.getCharts().get(0).getChartStateList().size());
	}

	@Test(expected = ConstraintViolationException.class)
	public void testChartStateWithTransientChartCreation() {
		Chart chart = new Chart("Airplay Charts");
		service.save(new ChartState(chart, getStartOfWeek(new Date())));
	}

	@Test(expected = ConstraintViolationException.class)
	public void testDuplicateChartCreation() {
		try {
			service.save(new Chart("Airplay Charts"));
		} catch (Exception exception) {
		}
		service.save(new Chart("Airplay Charts"));
	}

	@Test(expected = ConstraintViolationException.class)
	public void testDuplicateChartPositionCreation() {
		ChartState chartState = null;
		Song song = null;
		try {
			Chart chart = service.save(new Chart("Airplay Charts"));
			chartState = service.save(new ChartState(chart, getStartOfWeek(new Date())));
			Artist artist = service.save(new Artist("JACKSON, MICHAEL"));
			song = service.save(new Song(artist, "THRILLER"));
			service.save(new ChartPosition(chartState, song, 1));
		} catch (Exception exception) {
		}
		service.save(new ChartPosition(chartState, song, 1));
	}

	@Test(expected = ConstraintViolationException.class)
	public void testDuplicateChartStateCreation() {
		Chart chart = null;
		try {
			chart = service.save(new Chart("Airplay Charts"));
			service.save(new ChartState(chart, getStartOfWeek(new Date())));
			service.save(new ChartState(service.save(new Chart("Sales Charts")), getStartOfWeek(new Date())));
		} catch (Exception exception) {
		}
		service.save(new ChartState(chart, getStartOfWeek(new Date())));
	}

	@Test(expected = ConstraintViolationException.class)
	public void testEmptyNameChartCreation() {
		service.save(new Chart(""));
	}

	@Test(expected = ConstraintViolationException.class)
	public void testNullNameChartCreation() {
		service.save(new Chart(null));
	}

}
