package de.airsupply.airplay.core.model.test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import de.airsupply.airplay.core.model.Artist;
import de.airsupply.airplay.core.model.Publisher;
import de.airsupply.airplay.core.model.RecordCompany;
import de.airsupply.airplay.core.model.Song;
import de.airsupply.airplay.core.services.ContentService;
import de.airsupply.commons.core.util.CollectionUtils;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/applicationContext-test.xml" })
@Transactional
public class ContentServiceTest {

	@Autowired
	private ContentService service;

	@Test
	public void testArtistCreation() {
		service.save(new Artist("JACKSON, MICHAEL"));
	}

	@Test
	public void testArtistCreationWithSimilarNames() {
		service.save(new Artist("JACKSON, MICHAEL"));
		service.save(new Artist("JACKSON, MICHAEL FEAT. JANET JACKSON"));
	}

	@Test(expected = ConstraintViolationException.class)
	public void testDuplicateArtistCreation() {
		service.save(new Artist("JACKSON, MICHAEL"));
		service.save(new Artist("JACKSON, MICHAEL"));
	}

	@Test(expected = ConstraintViolationException.class)
	public void testDuplicatePublisherCreation() {
		service.save(new Publisher("MCA"));
		service.save(new Publisher("MCA"));
	}

	@Test(expected = ConstraintViolationException.class)
	public void testDuplicateRecordCompanyCreation() {
		service.save(new RecordCompany("EMI"));
		service.save(new RecordCompany("EMI"));
	}

	@Test(expected = ConstraintViolationException.class)
	public void testDuplicateSongCreation() {
		Artist artist = service.save(new Artist("JACKSON, MICHAEL"));
		service.save(new Song(artist, "THRILLER"));
		service.save(new Song(artist, "THRILLER"));
	}

	@Test(expected = ConstraintViolationException.class)
	public void testEmptyNameArtistCreation() {
		service.save(new Artist(""));
	}

	@Test(expected = ConstraintViolationException.class)
	public void testEmptyNamePublisherCreation() {
		service.save(new Publisher(""));
	}

	@Test(expected = ConstraintViolationException.class)
	public void testEmptyNameRecordCompanyCreation() {
		service.save(new RecordCompany(""));
	}

	@Test(expected = ConstraintViolationException.class)
	public void testEmptyNameSongCreation() {
		Artist artist = service.save(new Artist("JACKSON, MICHAEL"));
		service.save(new Song(artist, ""));
	}

	@Test(expected = ConstraintViolationException.class)
	public void testNullArtistSongCreation() {
		service.save(new Song(null, "THRILLER"));
	}

	@Test(expected = ConstraintViolationException.class)
	public void testNullNameArtistCreation() {
		service.save(new Artist(null));
	}

	@Test
	public void testNullNameArtistCreationValidation() {
		try {
			service.save(new Artist(null));
		} catch (ConstraintViolationException exception) {
			List<ConstraintViolation<?>> violations = CollectionUtils.asList(exception.getConstraintViolations());
			Assert.assertEquals(1, violations.size());
			Assert.assertEquals(null, violations.get(0).getInvalidValue());
		}
	}

	@Test
	public void testRepeatedArtistSave() {
		Artist artist = service.save(new Artist("JACKSON, MICHAEL"));
		service.save(artist);
	}

	@Test
	public void testRepeatedSongSave() {
		Artist artist = service.save(new Artist("JACKSON, MICHAEL"));
		Song song = service.save(new Song(artist, "THRILLER"));
		service.save(song);
	}

	@Test
	public void testSongCreation() {
		Artist artist = service.save(new Artist("JACKSON, MICHAEL"));
		Song song = service.save(new Song(artist, "THRILLER", "ABCDEFG"));
		artist = service.fetch(artist);

		assertArrayEquals(new Song[] { song }, artist.getSongList().toArray());
	}

	@Test
	public void testSongRetrieval() {
		Artist artist;
		Song song;

		artist = service.save(new Artist("JACKSON, MICHAEL"));
		song = service.save(new Song(artist, "THRILLER", "ABC"));
		artist = service.save(new Artist("PET SHOP BOYS, THE"));
		song = service.save(new Song(artist, "WHAT HAVE I DONE TO DESERVE THIS", "XYZ"));
		song = service.save(new Song(artist, "GO WEST", "UVW"));

		Artist queriedArtist = service.find(new Artist("PET SHOP BOYS, THE"));
		Song result = service.find(new Song(queriedArtist, "GO WEST"));

		assertEquals(song, result);
	}

	@Test
	public void testSongRetrievalByArtist() {
		Artist artist;

		@SuppressWarnings("unused")
		Song song;

		artist = service.save(new Artist("JACKSON, MICHAEL"));
		song = service.save(new Song(artist, "THRILLER", "ABC"));
		song = service.save(new Song(artist, "BEAT IT", "ABC"));

		List<Song> results = CollectionUtils.asList(service.findSongs(artist));

		assertEquals(2, results.size());
	}

	@Test
	public void testSongSearchWithAdvancedQuery() {
		Artist artist;
		Song song;

		List<Song> expectedResults = new ArrayList<>(4);

		artist = service.save(new Artist("JACKSON, MICHAEL"));
		song = service.save(new Song(artist, "THRILLER", "ABC"));
		song = service.save(new Song(artist, "BEAT IT", "ABC"));

		artist = service.save(new Artist("PRINCE"));
		song = service.save(new Song(artist, "KISS", "ABC"));

		artist = service.save(new Artist("U2"));
		song = service.save(new Song(artist, "KISS ME, KILL ME, THRILL ME", "ABC"));
		expectedResults.add(song);

		artist = service.save(new Artist("SIXPENCE NON THE RICHER"));
		song = service.save(new Song(artist, "KISS ME", "ABC"));
		expectedResults.add(song);

		artist = service.save(new Artist("KISS ME"));
		song = service.save(new Song(artist, "ABERTURA", "ABC"));
		expectedResults.add(song);
		song = service.save(new Song(artist, "RAINING DAY", "ABC"));
		expectedResults.add(song);

		List<Song> songs = service.findSongs("KISS* && ME*", true);

		assertArrayEquals(expectedResults.toArray(), songs.toArray());
	}

	@Test
	public void testSongSearchWithSimpleQuery() {
		Artist artist;
		Song song;

		List<Song> expectedResults = new ArrayList<>(4);

		artist = service.save(new Artist("JACKSON, MICHAEL"));
		song = service.save(new Song(artist, "THRILLER", "ABC"));
		song = service.save(new Song(artist, "BEAT IT", "ABC"));

		artist = service.save(new Artist("PRINCE"));
		song = service.save(new Song(artist, "KISS", "ABC"));

		artist = service.save(new Artist("U2"));
		song = service.save(new Song(artist, "KISS ME, KILL ME, THRILL ME", "ABC"));
		expectedResults.add(song);

		artist = service.save(new Artist("SIXPENCE NON THE RICHER"));
		song = service.save(new Song(artist, "KISS ME", "ABC"));
		expectedResults.add(song);

		artist = service.save(new Artist("KISS ME"));
		song = service.save(new Song(artist, "ABERTURA", "ABC"));
		expectedResults.add(song);
		song = service.save(new Song(artist, "RAINING DAY", "ABC"));
		expectedResults.add(song);

		List<Song> songs = service.findSongs("KISS ME", false);

		assertArrayEquals(expectedResults.toArray(), songs.toArray());
	}

	@Test(expected = ValidationException.class)
	public void testSongWithTransientArtistCreation() {
		Artist artist = new Artist("JACKSON, MICHAEL");
		service.save(new Song(artist, "THRILLER"));
	}

	@Test(expected = ConstraintViolationException.class)
	public void testSongWithTransientPublisherCreation() {
		Artist artist = service.save(new Artist("JACKSON, MICHAEL"));
		Publisher publisher = new Publisher("MCA");
		service.save(new Song(artist, "THRILLER", null, null, publisher));
	}

	@Test(expected = ConstraintViolationException.class)
	public void testSongWithTransientRecordCompanyCreation() {
		Artist artist = service.save(new Artist("JACKSON, MICHAEL"));
		RecordCompany recordCompany = new RecordCompany("EMI");
		service.save(new Song(artist, "THRILLER", null, recordCompany, null));
	}

}