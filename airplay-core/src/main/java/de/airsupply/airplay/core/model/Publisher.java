package de.airsupply.airplay.core.model;

import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.data.neo4j.annotation.Indexed;
import org.springframework.data.neo4j.annotation.NodeEntity;

import de.airsupply.commons.core.neo4j.annotation.Unique;

@Unique(parameters = { "name" })
@NodeEntity
@SuppressWarnings("serial")
public class Publisher extends PersistentNode {

	@NotEmpty
	@Indexed
	private String name;

	Publisher() {
		super();
	}

	public Publisher(String name) {
		super();
		this.name = name;
	}

	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return "Publisher [name=" + name + ", getIdentifier()=" + getIdentifier() + "]";
	}

}
