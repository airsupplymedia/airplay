package de.airsupply.airplay.core.model.test;

import static org.junit.Assert.assertArrayEquals;

import java.util.Date;

import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import de.airsupply.airplay.core.config.ApplicationConfiguration;
import de.airsupply.airplay.core.model.Artist;
import de.airsupply.airplay.core.model.Song;
import de.airsupply.airplay.core.model.SongBroadcast;
import de.airsupply.airplay.core.model.Station;
import de.airsupply.airplay.core.model.test.config.TestConfiguration;
import de.airsupply.airplay.core.services.StationService;
import de.airsupply.commons.core.util.DateUtils;

@RunWith(SpringJUnit4ClassRunner.class)
@ActiveProfiles("test")
@ContextConfiguration(classes = { ApplicationConfiguration.class, TestConfiguration.class })
@Transactional
public class StationServiceTest {

	@Autowired
	private StationService service;

	@Test(expected = ConstraintViolationException.class)
	public void testDuplicateSongBroadcastCreation() {
		Station station = service.save(new Station("SWR", null));
		Artist artist = service.save(new Artist("JACKSON, MICHAEL"));
		Song song = service.save(new Song(artist, "THRILLER"));
		Date time = new Date();
		service.save(new SongBroadcast(station, song, time));
		service.save(new SongBroadcast(station, song, time));
	}

	@Test(expected = ConstraintViolationException.class)
	public void testDuplicateStationCreation() {
		service.save(new Station("SWR", null));
		service.save(new Station("SWR", null));
	}

	@Test(expected = ConstraintViolationException.class)
	public void testEmptyNameStationCreation() {
		service.save(new Station("", null));
	}

	@Test(expected = ConstraintViolationException.class)
	public void testNullNameStationCreation() {
		service.save(new Station(null, null));
	}

	@Test
	public void testSongBroadcastCreation() {
		Station station = service.save(new Station("SWR", null));
		Artist artist = service.save(new Artist("JACKSON, MICHAEL"));
		Song song = service.save(new Song(artist, "THRILLER"));
		service.save(new SongBroadcast(station, song, new Date()));
	}

	@Test
	public void testSongBroadcastRetrieval() {
		Station station = service.save(new Station("SWR", null));
		Artist artist = service.save(new Artist("JACKSON, MICHAEL"));
		Song song = service.save(new Song(artist, "THRILLER"));
		SongBroadcast songBroadcast = service.save(new SongBroadcast(station, song, new Date()));
		Object[] result = service.findBroadcasts(song).toArray();
		assertArrayEquals(new Object[] { songBroadcast }, result);
	}

	@Test(expected = ConstraintViolationException.class)
	public void testSongBroadcastWithNullSongCreation() {
		Station station = service.save(new Station("SWR", null));
		service.save(new SongBroadcast(station, null, new Date()));
	}

	@Test(expected = ValidationException.class)
	public void testSongBroadcastWithTransientStationCreation() {
		Station station = new Station("SWR", null);
		Artist artist = service.save(new Artist("JACKSON, MICHAEL"));
		Song song = service.save(new Song(artist, "THRILLER"));
		service.save(new SongBroadcast(station, song, DateUtils.getStartOfWeek(new Date())));
	}

	@Test(expected = ConstraintViolationException.class)
	public void testSongBroadcastWithZeroCountCreation() {
		Station station = service.save(new Station("SWR", null));
		Artist artist = service.save(new Artist("JACKSON, MICHAEL"));
		Song song = service.save(new Song(artist, "THRILLER"));
		service.save(new SongBroadcast(station, song, new Date(), 0));
	}

	@Test
	public void testStationCreation() {
		service.save(new Station("SWR", null));
	}

}
