package de.airsupply.airplay.core.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.hibernate.validator.constraints.NotEmpty;
import org.neo4j.graphdb.Direction;
import org.springframework.data.neo4j.annotation.Indexed;
import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.data.neo4j.annotation.RelatedTo;

import de.airsupply.commons.core.neo4j.annotation.Unique;
import de.airsupply.commons.core.util.CollectionUtils;

@Unique(arguments = { "name" })
@NodeEntity
@SuppressWarnings("serial")
public class Station extends PersistentNode {

	@Indexed
	private String longName;

	@NotEmpty
	@Indexed
	private String name;

	@RelatedTo(direction = Direction.BOTH, type = "SHOWS")
	private Iterable<Show> shows = new HashSet<>();

	Station() {
		super();
	}

	public Station(String name, String longName) {
		super();
		this.name = name;
		this.longName = longName;
	}

	public String getLongName() {
		return longName;
	}

	public String getName() {
		return name;
	}

	public List<Show> getShowList() {
		if (shows != null) {
			return CollectionUtils.asList(shows);
		} else {
			return Collections.emptyList();
		}
	}

	@Override
	public String toString() {
		return "Station [name=" + name + ", getIdentifier()=" + getIdentifier() + "]";
	}

}
