package de.airsupply.airplay.core.graph.repository;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.CypherDslRepository;
import org.springframework.data.neo4j.repository.GraphRepository;

import de.airsupply.airplay.core.model.Show;
import de.airsupply.airplay.core.model.ShowBroadcast;

public interface ShowBroadcastRepository extends GraphRepository<ShowBroadcast>, CypherDslRepository<ShowBroadcast> {

	@Query("START broadcastedShow=node({0}) MATCH broadcastedShow-[showBroadcast:SHOW_BROADCAST]->() RETURN showBroadcast")
	Iterable<ShowBroadcast> find(Show show);

}
