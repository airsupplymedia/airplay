package de.airsupply.airplay.core.graph.repository;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.GraphRepository;

import de.airsupply.airplay.core.model.Chart;
import de.airsupply.airplay.core.model.ChartState;

public interface ChartStateRepository extends GraphRepository<ChartState> {

	@Query("START chart=node({0}) MATCH chart<-[:CHART_STATES]->chartState RETURN chartState ORDER BY chartState.week DESC LIMIT 1")
	ChartState findLatest(Chart chart);

	@Query("START chart=node({0}) MATCH chart<-[:CHART_STATES]->chartState WHERE chartState.week={1} RETURN chartState")
	ChartState find(Chart chart, long week);

}
