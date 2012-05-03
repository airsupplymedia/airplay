package de.airsupply.commons.core.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.neo4j.helpers.collection.ClosableIterable;
import org.springframework.util.Assert;

public abstract class CollectionUtils {

	public static <T extends Object> List<T> asList(ClosableIterable<T> iterable) {
		Assert.notNull(iterable);
		return asList(iterable, null);
	}

	public static <T extends Object, F extends Object> List<F> asList(ClosableIterable<T> iterable, Class<F> type) {
		Assert.notNull(iterable);
		List<F> result = asList((Iterable<T>) iterable, type);
		iterable.close();
		return result;
	}

	public static <T extends Object> List<T> asList(Iterable<T> iterable) {
		Assert.notNull(iterable);
		return asList(iterable, null);
	}

	@SuppressWarnings("unchecked")
	public static <T extends Object, F extends Object> List<F> asList(Iterable<T> iterable, Class<F> type) {
		Assert.notNull(iterable);
		Iterator<T> iterator = iterable.iterator();
		List<F> result = new ArrayList<>();
		while (iterator.hasNext()) {
			T next = iterator.next();
			if (type == null) {
				result.add((F) next);
			} else if (type.isInstance(next)) {
				result.add(type.cast(next));
			}
		}
		return Collections.unmodifiableList(result);
	}

	public static <T extends Object> Set<T> asSet(ClosableIterable<T> iterable) {
		Assert.notNull(iterable);
		return asSet(iterable, null);
	}

	public static <T extends Object, F extends Object> Set<F> asSet(ClosableIterable<T> iterable, Class<F> type) {
		Assert.notNull(iterable);
		Set<F> result = asSet((Iterable<T>) iterable, type);
		iterable.close();
		return result;
	}

	public static <T extends Object> Set<T> asSet(Iterable<T> iterable) {
		Assert.notNull(iterable);
		return asSet(iterable, null);
	}

	@SuppressWarnings("unchecked")
	public static <T extends Object, F extends Object> Set<F> asSet(Iterable<T> iterable, Class<F> type) {
		Assert.notNull(iterable);
		Iterator<T> iterator = iterable.iterator();
		Set<F> result = new HashSet<>();
		while (iterator.hasNext()) {
			T next = iterator.next();
			if (type == null) {
				result.add((F) next);
			} else if (type.isInstance(next)) {
				result.add(type.cast(next));
			}
		}
		return Collections.unmodifiableSet(result);
	}

}
