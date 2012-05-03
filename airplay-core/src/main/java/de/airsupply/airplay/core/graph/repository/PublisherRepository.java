package de.airsupply.airplay.core.graph.repository;

import org.springframework.data.neo4j.repository.GraphRepository;

import de.airsupply.airplay.core.model.Publisher;

public interface PublisherRepository extends GraphRepository<Publisher> {
}
