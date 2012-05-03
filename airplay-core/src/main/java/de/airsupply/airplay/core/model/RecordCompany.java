package de.airsupply.airplay.core.model;

import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.data.neo4j.annotation.Indexed;
import org.springframework.data.neo4j.annotation.NodeEntity;

import de.airsupply.commons.core.neo4j.annotation.Unique;

@Unique(arguments = { "name" })
@NodeEntity
@SuppressWarnings("serial")
public class RecordCompany extends PersistentNode {

	@NotEmpty
	@Indexed
	private String name;

	RecordCompany() {
		super();
	}

	public RecordCompany(String name) {
		super();
		this.name = name;
	}

	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return "RecordCompany [name=" + name + ", getIdentifier()=" + getIdentifier() + "]";
	}

}
