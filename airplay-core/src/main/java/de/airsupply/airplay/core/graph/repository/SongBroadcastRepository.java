package de.airsupply.airplay.core.graph.repository;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.CypherDslRepository;
import org.springframework.data.neo4j.repository.GraphRepository;

import de.airsupply.airplay.core.model.Song;
import de.airsupply.airplay.core.model.SongBroadcast;

public interface SongBroadcastRepository extends GraphRepository<SongBroadcast>, CypherDslRepository<SongBroadcast> {

	@Query("START broadcastedSong=node({0}) MATCH broadcastedSong<-[:SONG_BROADCAST_OF]-songBroadcast RETURN songBroadcast")
	Iterable<SongBroadcast> find(Song song);

}
