package de.airsupply.airplay.core.graph.repository;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.CypherDslRepository;
import org.springframework.data.neo4j.repository.GraphRepository;

import de.airsupply.airplay.core.model.Chart;
import de.airsupply.airplay.core.model.ChartPosition;
import de.airsupply.airplay.core.model.Song;

public interface ChartPositionRepository extends GraphRepository<ChartPosition>, CypherDslRepository<ChartPosition> {

	@Query("START chart=node({0}) MATCH chart-[:CHART_STATES]->chartState-[chartPosition:CHART_POSITIONS]->() WHERE chartState.week={1} RETURN chartPosition ORDER BY chartPosition.position")
	Iterable<ChartPosition> find(Chart chart, long week);

	@Query("START chart=node({0}), song=node({1}) MATCH chart-[:CHART_STATES]->chartState-[chartPosition:CHART_POSITIONS]->song RETURN chartPosition ORDER BY chartState.week DESC")
	Iterable<ChartPosition> find(Chart chart, Song song);

}
