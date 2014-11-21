package de.airsupply.airplay.core.graph.repository;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.GraphRepository;

import de.airsupply.airplay.core.model.Artist;

public interface ArtistRepository extends GraphRepository<Artist> {

	@Query("START song=node:searchArtistByName({0}) RETURN artist ORDER BY artist.name")
	Iterable<Artist> findByName(String query);

}
