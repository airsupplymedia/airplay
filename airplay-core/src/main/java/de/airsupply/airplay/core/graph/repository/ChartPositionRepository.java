package de.airsupply.airplay.core.graph.repository;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.CypherDslRepository;
import org.springframework.data.neo4j.repository.GraphRepository;

import de.airsupply.airplay.core.model.Chart;
import de.airsupply.airplay.core.model.ChartPosition;
import de.airsupply.airplay.core.model.ChartState;
import de.airsupply.airplay.core.model.Song;

public interface ChartPositionRepository extends GraphRepository<ChartPosition>, CypherDslRepository<ChartPosition> {

	@Query("START chartState=node({0}) MATCH chartState-[:CHART_POSITIONS]-chartPosition RETURN chartPosition ORDER BY chartPosition.position")
	Iterable<ChartPosition> find(ChartState chartState);

	@Query("START chart=node({0}), song=node({1}) MATCH song<-[:CHART_POSITION]-chartPosition-[:CHART_POSITIONS]-chartState<-[:CHART_STATES]->chart RETURN chartPosition ORDER BY chartState.week DESC")
	Iterable<ChartPosition> find(Chart chart, Song song);

}
