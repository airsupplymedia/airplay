package de.airsupply.airplay.core.model;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Collections;
import java.util.List;

import org.hibernate.validator.constraints.NotEmpty;
import org.neo4j.graphdb.Direction;
import org.springframework.data.neo4j.annotation.Indexed;
import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.data.neo4j.annotation.RelatedTo;
import org.springframework.data.neo4j.support.index.IndexType;

import com.fasterxml.jackson.annotation.JsonIgnore;

import de.airsupply.commons.core.neo4j.annotation.Unique;
import de.airsupply.commons.core.util.CollectionUtils;

@Unique(query = "START artist=node:searchArtistByName({:name}) WHERE LOWER(artist.name)=LOWER({name}) RETURN artist", parameters = {
		"name", ":name" })
@NodeEntity
@SuppressWarnings("serial")
public class Artist extends PersistentNode {

	@NotEmpty
	@Indexed(indexType = IndexType.FULLTEXT, indexName = "searchArtistByName")
	private String name;

	@RelatedTo(direction = Direction.BOTH, type = "SONGS")
	@JsonIgnore
	private Iterable<Song> songs = null;

	Artist() {
		super();
	}

	public Artist(String name) {
		super();
		this.name = name;
	}

	public String getName() {
		return name;
	}

	@JsonIgnore
	public List<Song> getSongList() {
		if (songs != null) {
			return CollectionUtils.asList(songs);
		} else {
			return Collections.emptyList();
		}
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return "Artist [name=" + name + ", getIdentifier()=" + getIdentifier() + "]";
	}

	private void writeObject(ObjectOutputStream outputStream) throws IOException {
		songs = null;
		outputStream.defaultWriteObject();
	}

}
