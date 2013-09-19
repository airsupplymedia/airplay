package de.airsupply.airplay.core.services;

import java.util.ArrayList;
import java.util.Collections;
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
import de.airsupply.commons.core.util.CollectionUtils;

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
		return CollectionUtils.asList(artistRepository.findAllByQuery("name", query));
	}

	public Publisher findPublisher(Publisher object) {
		Assert.notNull(object);
		return publisherRepository.findByPropertyValue("name", object.getName());
	}

	public Publisher findPublisher(String name) {
		Assert.hasText(name);
		return findPublisher(new Publisher(name));
	}

	public RecordCompany findRecordCompany(RecordCompany object) {
		Assert.notNull(object);
		return recordCompanyRepository.findByPropertyValue("name", object.getName());
	}

	public RecordCompany findRecordCompany(String name) {
		Assert.hasText(name);
		return findRecordCompany(new RecordCompany(name));
	}

	public List<Song> findSongs(Artist artist) {
		Assert.notNull(artist);
		return CollectionUtils.asList(songRepository.findByArtist(artist));
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
		result.addAll(CollectionUtils.asList(songRepository.findAllByQuery("name", search)));
		result.addAll(CollectionUtils.asList(songRepository.findByArtistName(searchWithFields)));
		return Collections.unmodifiableList(result);
	}

	public long getArtistCount() {
		return artistRepository.count();
	}

	public List<Artist> getArtists() {
		return CollectionUtils.asList(artistRepository.findAll());
	}

	public long getPublisherCount() {
		return publisherRepository.count();
	}

	public List<Publisher> getPublishers() {
		return CollectionUtils.asList(publisherRepository.findAll());
	}

	public List<RecordCompany> getRecordCompanies() {
		return CollectionUtils.asList(recordCompanyRepository.findAll());
	}

	public long getRecordCompanyCount() {
		return recordCompanyRepository.count();
	}

	public long getSongCount() {
		return songRepository.count();
	}

	public List<Song> getSongs() {
		return CollectionUtils.asList(songRepository.findAll());
	}

	public Iterable<Song> getSongs(Iterable<Long> ids) {
		return songRepository.findAll(ids);
	}

	public boolean hasSong(Long id) {
		return songRepository.exists(id);
	}

}