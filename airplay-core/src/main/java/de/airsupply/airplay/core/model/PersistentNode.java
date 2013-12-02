package de.airsupply.airplay.core.model;

import org.joda.time.DateTime;
import org.springframework.data.domain.Auditable;
import org.springframework.data.neo4j.annotation.GraphId;
import org.springframework.util.Assert;

import com.fasterxml.jackson.annotation.JsonIgnore;

@SuppressWarnings("serial")
public class PersistentNode implements Auditable<User, Long> {

	public static final String ID_NAME = "identifier";

	@JsonIgnore
	private long createdDate;

	@GraphId
	private Long identifier;

	@JsonIgnore
	private long lastModifiedDate;

	public PersistentNode() {
		super();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (identifier == null) {
			return false;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		PersistentNode other = (PersistentNode) obj;
		if (!identifier.equals(other.identifier)) {
			return false;
		}
		return true;
	}

	@Override
	@JsonIgnore
	public User getCreatedBy() {
		throw new UnsupportedOperationException();
	}

	@Override
	@JsonIgnore
	public DateTime getCreatedDate() {
		return new DateTime(createdDate);
	}

	@Override
	@JsonIgnore
	public Long getId() {
		return getIdentifier();
	}

	public Long getIdentifier() {
		return identifier;
	}

	@Override
	@JsonIgnore
	public User getLastModifiedBy() {
		throw new UnsupportedOperationException();
	}

	@Override
	@JsonIgnore
	public DateTime getLastModifiedDate() {
		return new DateTime(lastModifiedDate);
	}

	@Override
	public int hashCode() {
		if (identifier == null) {
			return System.identityHashCode(this);
		}
		final int prime = 31;
		int result = 1;
		result = prime * result + ((identifier == null) ? 0 : identifier.hashCode());
		return result;
	}

	@JsonIgnore
	@Override
	public boolean isNew() {
		return identifier == null;
	}

	@Override
	@JsonIgnore
	public void setCreatedBy(User createdBy) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setCreatedDate(DateTime createdDate) {
		Assert.notNull(createdDate);
		this.createdDate = createdDate.getMillis();
		setLastModifiedDate(createdDate);
	}

	@Override
	@JsonIgnore
	public void setLastModifiedBy(User lastModifiedBy) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setLastModifiedDate(DateTime lastModifiedDate) {
		Assert.notNull(lastModifiedDate);
		Assert.isTrue(lastModifiedDate.isAfter(createdDate) || lastModifiedDate.isEqual(createdDate));
		this.lastModifiedDate = lastModifiedDate.getMillis();
	}

}
