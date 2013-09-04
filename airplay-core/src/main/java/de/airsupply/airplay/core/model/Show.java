package de.airsupply.airplay.core.model;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;
import org.neo4j.graphdb.Direction;
import org.springframework.data.neo4j.annotation.Indexed;
import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.data.neo4j.annotation.RelatedTo;

import de.airsupply.commons.core.neo4j.annotation.Persistent;
import de.airsupply.commons.core.neo4j.annotation.Unique;

@Unique(query = "START station=node({station}) MATCH station<-[:SHOWS]->show WHERE show.name={name} RETURN show", arguments = {
		"station", "name" })
@NodeEntity
@SuppressWarnings("serial")
public class Show extends PersistentNode {

	@NotEmpty
	@Indexed
	private String name;

	@NotNull
	@Persistent
	@RelatedTo(direction = Direction.OUTGOING, type = "STATION")
	private Station station;

	Show() {
		super();
	}

	public Show(Station station, String name) {
		super();
		this.station = station;
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public Station getStation() {
		return station;
	}

	@Override
	public String toString() {
		return "Show [name=" + name + ", station=" + station + ", getIdentifier()=" + getIdentifier() + "]";
	}

}
