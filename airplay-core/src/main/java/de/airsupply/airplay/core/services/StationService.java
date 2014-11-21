package de.airsupply.airplay.core.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import de.airsupply.airplay.core.graph.repository.ShowBroadcastRepository;
import de.airsupply.airplay.core.graph.repository.ShowRepository;
import de.airsupply.airplay.core.graph.repository.SongBroadcastRepository;
import de.airsupply.airplay.core.graph.repository.StationRepository;
import de.airsupply.airplay.core.model.Show;
import de.airsupply.airplay.core.model.ShowBroadcast;
import de.airsupply.airplay.core.model.Song;
import de.airsupply.airplay.core.model.SongBroadcast;
import de.airsupply.airplay.core.model.Station;
import de.airsupply.commons.core.neo4j.Neo4jServiceSupport;
import de.airsupply.commons.core.util.CollectionUtils;

@Service
public class StationService extends Neo4jServiceSupport {

	@Autowired
	private ShowBroadcastRepository showBroadcastRepository;

	@Autowired
	private ShowRepository showRepository;

	@Autowired
	private SongBroadcastRepository songBroadcastRepository;

	@Autowired
	private StationRepository stationRepository;

	public List<ShowBroadcast> findBroadcasts(Show show) {
		Assert.notNull(show);
		return CollectionUtils.asList(showBroadcastRepository.find(show));
	}

	public List<SongBroadcast> findBroadcasts(Song song) {
		Assert.notNull(song);
		return CollectionUtils.asList(songBroadcastRepository.find(song));
	}

	public List<Station> findStations(Station object) {
		Assert.notNull(object);
		return CollectionUtils.asList(stationRepository.findAllBySchemaPropertyValue("name", object.getName()));
	}

	public List<Station> findStations(String name) {
		Assert.hasText(name);
		return findStations(new Station(name));
	}

	public long getShowBroadcastCount() {
		return showBroadcastRepository.count();
	}

	public List<ShowBroadcast> getShowBroadcasts() {
		return CollectionUtils.asList(showBroadcastRepository.findAll());
	}

	public long getShowCount() {
		return showRepository.count();
	}

	public List<Show> getShows() {
		return CollectionUtils.asList(showRepository.findAll());
	}

	public long getSongBroadcastCount() {
		return songBroadcastRepository.count();
	}

	public List<SongBroadcast> getSongBroadcasts() {
		return CollectionUtils.asList(songBroadcastRepository.findAll());
	}

	public long getStationCount() {
		return stationRepository.count();
	}

	public List<Station> getStations() {
		return CollectionUtils.asList(stationRepository.findAll());
	}

}
