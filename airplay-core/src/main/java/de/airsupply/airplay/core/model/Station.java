package de.airsupply.airplay.core.model;

import java.io.IOException;
import java.io.ObjectOutputStream;

import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.data.neo4j.annotation.Indexed;
import org.springframework.data.neo4j.annotation.NodeEntity;

import de.airsupply.commons.core.neo4j.annotation.Unique;

@Unique(arguments = { "name" })
@NodeEntity
@SuppressWarnings("serial")
public class Station extends PersistentNode {

	@Indexed
	private String longName;

	@NotEmpty
	@Indexed
	private String name;

	Station() {
		super();
	}

	public Station(String name) {
		super();
		this.name = name;
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

	@Override
	public String toString() {
		return "Station [name=" + name + ", longName=" + longName + "]";
	}

	private void writeObject(ObjectOutputStream outputStream) throws IOException {
		outputStream.defaultWriteObject();
	}

}
