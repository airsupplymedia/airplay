package de.airsupply.airplay.core.services;

import static de.airsupply.commons.core.util.CollectionUtils.asList;
import static java.util.Collections.unmodifiableList;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import de.airsupply.airplay.core.graph.repository.ArtistRepository;
import de.airsupply.airplay.core.graph.repository.PublisherRepository;
import de.airsupply.airplay.core.graph.repository.RecordCompanyRepository;
import de.airsupply.airplay.core.graph.repository.SongRepository;
import de.airsupply.airplay.core.model.Artist;
import de.airsupply.airplay.core.model.Publisher;
import de.airsupply.airplay.core.model.RecordCompany;
import de.airsupply.airplay.core.model.Song;
import de.airsupply.commons.core.neo4j.Neo4jServiceSupport;
import de.airsupply.commons.core.neo4j.QueryUtils;

@Service
public class ContentService extends Neo4jServiceSupport {

	@Autowired
	private ArtistRepository artistRepository;

	@Autowired
	private PublisherRepository publisherRepository;

	@Autowired
	private RecordCompanyRepository recordCompanyRepository;

	@Autowired
	private SongRepository songRepository;

	public List<Artist> findArtists(String query, boolean advancedSearch) {
		Assert.notNull(query);
		if (!advancedSearch) {
			query = QueryUtils.buildDefaultQuery(query);
		}
		return asList(artistRepository.findAllByQuery("name", query));
	}

	public List<Publisher> findPublishers(Publisher object) {
		Assert.notNull(object);
		return asList(publisherRepository.findAllByPropertyValue("name", object.getName()));
	}

	public List<Publisher> findPublishers(String name) {
		Assert.hasText(name);
		return findPublishers(new Publisher(name));
	}

	public List<RecordCompany> findRecordCompanies(RecordCompany object) {
		Assert.notNull(object);
		return asList(recordCompanyRepository.findAllByPropertyValue("name", object.getName()));
	}

	public List<RecordCompany> findRecordCompanies(String name) {
		Assert.hasText(name);
		return findRecordCompanies(new RecordCompany(name));
	}

	public List<Song> findSongs(Artist artist) {
		Assert.notNull(artist);
		return asList(songRepository.findByArtist(artist));
	}

	public List<Song> findSongs(String query, boolean advancedSearch) {
		Assert.notNull(query);
		String search;
		String searchWithFields;
		if (!advancedSearch) {
			search = QueryUtils.buildDefaultQuery(query);
			searchWithFields = QueryUtils.buildDefaultQuery(query, "name");
		} else {
			search = query;
			searchWithFields = query;
		}
		// FIXME Use Single Cypher query to increase performance
		List<Song> result = new ArrayList<>();
		result.addAll(asList(songRepository.findAllByQuery("name", search)));
		result.addAll(asList(songRepository.findByArtistName(searchWithFields)));
		return unmodifiableList(result);
	}

	public long getArtistCount() {
		return artistRepository.count();
	}

	public List<Artist> getArtists() {
		return asList(artistRepository.findAll());
	}

	public long getPublisherCount() {
		return publisherRepository.count();
	}

	public List<Publisher> getPublishers() {
		return asList(publisherRepository.findAll());
	}

	public List<RecordCompany> getRecordCompanies() {
		return asList(recordCompanyRepository.findAll());
	}

	public long getRecordCompanyCount() {
		return recordCompanyRepository.count();
	}

	public long getSongCount() {
		return songRepository.count();
	}

	public List<Song> getSongs() {
		return asList(songRepository.findAll());
	}

	public Iterable<Song> getSongs(Iterable<Long> ids) {
		return songRepository.findAll(ids);
	}

	public boolean hasSong(Long id) {
		return songRepository.exists(id);
	}

}