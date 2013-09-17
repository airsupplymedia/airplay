package de.airsupply.airplay.core.model;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;
import org.neo4j.graphdb.Direction;
import org.springframework.data.neo4j.annotation.Fetch;
import org.springframework.data.neo4j.annotation.Indexed;
import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.data.neo4j.annotation.RelatedTo;
import org.springframework.data.neo4j.support.index.IndexType;

import de.airsupply.commons.core.neo4j.annotation.Persistent;
import de.airsupply.commons.core.neo4j.annotation.Unique;

@Unique(query = "START artist=node({artist}) MATCH artist<-[:SONGS]->song WHERE LOWER(song.name)=LOWER({name}) RETURN song", arguments = {
		"artist", "name" })
@NodeEntity
@SuppressWarnings("serial")
public class Song extends PersistentNode {

	@Fetch
	@NotNull
	@Persistent
	@RelatedTo(direction = Direction.BOTH, type = "SONGS")
	private Artist artist;

	private String discIdentifier;

	@NotEmpty
	@Indexed(indexType = IndexType.FULLTEXT, indexName = "searchSongByName")
	private String name;

	@Fetch
	@Persistent
	@RelatedTo(direction = Direction.OUTGOING, type = "PUBLISHER")
	private Publisher publisher;

	@Fetch
	@Persistent
	@RelatedTo(direction = Direction.OUTGOING, type = "RECORD_COMPANY")
	private RecordCompany recordCompany;

	Song() {
		super();
	}

	public Song(Artist artist, String name) {
		super();
		this.artist = artist;
		this.name = name;
	}

	public Song(Artist artist, String name, String discIdentifier) {
		super();
		this.artist = artist;
		this.name = name;
		this.discIdentifier = discIdentifier;
	}

	public Song(Artist artist, String name, String discIdentifier, RecordCompany recordCompany, Publisher publisher) {
		super();
		this.artist = artist;
		this.name = name;
		this.discIdentifier = discIdentifier;
		this.recordCompany = recordCompany;
		this.publisher = publisher;
	}

	public Artist getArtist() {
		return artist;
	}

	public String getDiscIdentifier() {
		return discIdentifier;
	}

	public String getName() {
		return name;
	}

	public Publisher getPublisher() {
		return publisher;
	}

	public RecordCompany getRecordCompany() {
		return recordCompany;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return "Song [artist=" + artist + ", discIdentifier=" + discIdentifier + ", publisher=" + publisher
				+ ", recordCompany=" + recordCompany + ", name=" + name + ", getIdentifier()=" + getIdentifier() + "]";
	}

}
