package de.airsupply.airplay.core.graph.repository;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.CypherDslRepository;
import org.springframework.data.neo4j.repository.GraphRepository;

import de.airsupply.airplay.core.model.Artist;
import de.airsupply.airplay.core.model.Song;

public interface SongRepository extends GraphRepository<Song>, CypherDslRepository<Song> {

	@Query("START artist=node({0}) MATCH artist<-[:SONGS]->song RETURN song ORDER BY song.name")
	Iterable<Song> findByArtist(Artist artist);

}
