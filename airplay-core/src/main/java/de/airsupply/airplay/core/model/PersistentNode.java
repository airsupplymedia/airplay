package de.airsupply.airplay.core.model;

import java.io.Serializable;

import org.springframework.data.neo4j.annotation.GraphId;

@SuppressWarnings("serial")
public class PersistentNode implements Serializable {

	@GraphId
	private Long identifier;

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

	public Long getIdentifier() {
		return identifier;
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

	public boolean isPersistent() {
		return getIdentifier() != null;
	}

}
