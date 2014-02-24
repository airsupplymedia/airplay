package de.airsupply.airplay.core.model;

import java.util.Comparator;

import org.joda.time.DateTime;
import org.springframework.data.domain.Auditable;
import org.springframework.data.neo4j.annotation.GraphId;
import org.springframework.util.Assert;

import com.fasterxml.jackson.annotation.JsonIgnore;

@SuppressWarnings("serial")
public class PersistentNode implements Auditable<User, Long> {

	private static class PersistentNodeIdentifierComparator implements Comparator<PersistentNode> {

		@Override
		public int compare(PersistentNode o1, PersistentNode o2) {
			if (o1.isNew() && o2.isNew()) {
				return 0;
			} else if (o1.isNew()) {
				return -1;
			} else if (o2.isNew()) {
				return 1;
			}
			return o1.getIdentifier().compareTo(o2.getIdentifier());
		}

	}

	private static Comparator<PersistentNode> comparator = new PersistentNodeIdentifierComparator();

	public static final String ID_NAME = "identifier";

	public static Comparator<PersistentNode> identifierComparator() {
		if (comparator == null) {
			comparator = new PersistentNodeIdentifierComparator();
		}
		return comparator;
	}

	@JsonIgnore
	private long createdDate;

	@GraphId
	private Long identifier;

	@JsonIgnore
	private long lastModifiedDate;

	private boolean system;

	public PersistentNode() {
		super();
	}

	public PersistentNode(boolean system) {
		super();
		this.system = system;
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

	@JsonIgnore
	public boolean isSystem() {
		return system;
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

	@JsonIgnore
	public void setSystem(boolean system) {
		this.system = system;
	}

}
