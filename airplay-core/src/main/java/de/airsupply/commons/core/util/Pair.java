package de.airsupply.commons.core.util;

public class Pair<F extends Object, S extends Object> {

	private final F first;

	private final S second;

	public Pair(F first, S second) {
		this.first = first;
		this.second = second;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof Pair)) {
			return false;
		}
		@SuppressWarnings("unchecked")
		Pair<F, S> other = (Pair<F, S>) obj;
		if (first == null) {
			if (other.first != null) {
				return false;
			}
		} else if (!first.equals(other.first)) {
			return false;
		}
		if (second == null) {
			if (other.second != null) {
				return false;
			}
		} else if (!second.equals(other.second)) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((first == null) ? 0 : first.hashCode());
		result = prime * result + ((second == null) ? 0 : second.hashCode());
		return result;
	}

}