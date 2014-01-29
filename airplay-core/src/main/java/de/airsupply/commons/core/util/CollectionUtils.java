package de.airsupply.commons.core.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.neo4j.graphdb.index.IndexHits;
import org.neo4j.helpers.collection.ClosableIterable;
import org.springframework.data.neo4j.conversion.EndResult;
import org.springframework.util.Assert;

public abstract class CollectionUtils {

	public static interface Filter<T> {

		boolean accept(T object);

	}

	public static interface Function<S, T> {

		T apply(S source);

	}

	public static interface Procedure<T> {

		void run(T object);

	}

	public static Object[] asArray(Object... objects) {
		return objects;
	}

	public static <T> Iterable<T> asIterable(Iterator<T> iterator) {
		Assert.notNull(iterator);
		Collection<T> collection = new ArrayList<>();
		while (iterator.hasNext()) {
			collection.add(iterator.next());
		}
		return Collections.unmodifiableCollection(collection);
	}

	public static <T> List<T> asList(ClosableIterable<T> iterable) {
		Assert.notNull(iterable);
		return asList(iterable, null);
	}

	public static <T, F> List<F> asList(ClosableIterable<T> iterable, Class<F> type) {
		Assert.notNull(iterable);
		List<F> result = asList((Iterable<T>) iterable, type);
		iterable.close();
		return result;
	}

	public static <T> List<T> asList(Iterable<T> iterable) {
		Assert.notNull(iterable);
		return asList(iterable, null);
	}

	public static <T, F> List<F> asList(Iterable<T> iterable, boolean modifiable) {
		Assert.notNull(iterable);
		return asList(iterable, null, modifiable);
	}

	public static <T, F> List<F> asList(Iterable<T> iterable, Class<F> type) {
		Assert.notNull(iterable);
		return asList(iterable, type, false);
	}

	@SuppressWarnings("unchecked")
	public static <T, F> List<F> asList(Iterable<T> iterable, Class<F> type, boolean modifiable) {
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
		if (iterable instanceof EndResult) {
			((EndResult<T>) iterable).finish();
		}
		if (iterable instanceof IndexHits) {
			((IndexHits<T>) iterable).close();
		}
		if (!modifiable) {
			result = Collections.unmodifiableList(result);
		}
		return result;
	}

	public static <T> List<T> asModifiableList(Iterable<T> iterable) {
		Assert.notNull(iterable);
		return asList(iterable, null, true);
	}

	public static <T> Set<T> asSet(ClosableIterable<T> iterable) {
		Assert.notNull(iterable);
		return asSet(iterable, null);
	}

	public static <T, F> Set<F> asSet(ClosableIterable<T> iterable, Class<F> type) {
		Assert.notNull(iterable);
		Set<F> result = asSet((Iterable<T>) iterable, type);
		iterable.close();
		return result;
	}

	public static <T> Set<T> asSet(Iterable<T> iterable) {
		Assert.notNull(iterable);
		return asSet(iterable, null);
	}

	@SuppressWarnings("unchecked")
	public static <T, F> Set<F> asSet(Iterable<T> iterable, Class<F> type) {
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

	public static <S, T extends S> List<S> filter(Collection<S> source, Collection<T> target, Filter<T> filter) {
		Assert.notNull(source);
		Assert.notNull(target);
		Assert.notNull(filter);
		return filter(source, target, filter);
	}

	public static <S, T extends S> List<S> filter(Collection<S> list, Filter<T> filter) {
		Assert.notNull(list);
		Assert.notNull(filter);
		return filter(list, new ArrayList<S>(list.size()), filter);
	}

	@SuppressWarnings("unchecked")
	private static <S, T extends S, TC extends Collection<S>> TC filter(Iterable<S> source, TC target, Filter<T> filter) {
		Assert.isTrue(target.isEmpty());
		for (S object : source) {
			if (filter == null || filter.accept((T) object)) {
				target.add((S) object);
			}
		}
		return target;
	}

	public static <S, T extends S> List<S> filter(List<S> list, Filter<T> filter) {
		Assert.notNull(list);
		Assert.notNull(filter);
		return filter(list, new ArrayList<S>(list.size()), filter);
	}

	public static <S, T extends S> Set<S> filter(Set<S> set, Filter<T> filter) {
		Assert.notNull(set);
		Assert.notNull(filter);
		return filter(set, new HashSet<S>(set.size()), filter);
	}

	public static <T> Filter<T> filterFor(final Class<? extends T> type) {
		Assert.notNull(type);
		return new Filter<T>() {

			@Override
			public boolean accept(T object) {
				return type.isInstance(object);
			}

		};
	}

	@SafeVarargs
	public static <T> Filter<T> filterStrictly(final Filter<T>... filters) {
		return new Filter<T>() {

			@Override
			public boolean accept(T object) {
				for (Filter<T> filter : filters) {
					if (!filter.accept(object)) {
						return false;
					}
				}
				return true;
			}

		};
	}

	public static <T> void run(Collection<? extends T> collection, Procedure<T> procedure) {
		Assert.notNull(collection);
		Assert.notNull(procedure);
		for (T object : collection) {
			procedure.run(object);
		}
	}

	public static <S, T> List<T> transform(ClosableIterable<? extends S> iterable, Function<S, T> function) {
		Assert.notNull(iterable);
		Assert.notNull(function);
		return transform(iterable, new ArrayList<T>(), function, true);
	}

	public static <S, T> List<T> transform(ClosableIterable<? extends S> iterable, Function<S, T> function,
			boolean includeNullReferences) {
		Assert.notNull(iterable);
		Assert.notNull(function);
		return transform(iterable, new ArrayList<T>(), function, includeNullReferences);
	}

	public static <S, T> List<T> transform(Collection<? extends S> list, Function<S, T> function) {
		Assert.notNull(list);
		Assert.notNull(function);
		return transform(list, new ArrayList<T>(list.size()), function, true);
	}

	public static <S, T> List<T> transform(Collection<? extends S> list, Function<S, T> function,
			boolean includeNullReferences) {
		Assert.notNull(list);
		Assert.notNull(function);
		return transform(list, new ArrayList<T>(list.size()), function, includeNullReferences);
	}

	public static <S, T> List<T> transform(Iterable<? extends S> iterable, Function<S, T> function) {
		Assert.notNull(iterable);
		Assert.notNull(function);
		return transform(iterable, new ArrayList<T>(), function, true);
	}

	public static <S, T> List<T> transform(Iterable<? extends S> iterable, Function<S, T> function,
			boolean includeNullReferences) {
		Assert.notNull(iterable);
		Assert.notNull(function);
		return transform(iterable, new ArrayList<T>(), function, includeNullReferences);
	}

	private static <S, T, TC extends Collection<T>> TC transform(Iterable<? extends S> source, TC target,
			Function<S, T> function, boolean includeNullReferences) {
		Assert.isTrue(target.isEmpty());
		for (S object : source) {
			T transformed = function.apply(object);
			if (transformed != null || (transformed == null && includeNullReferences)) {
				target.add(transformed);
			}
		}
		return target;
	}

	public static <S, T> List<T> transform(List<? extends S> list, Function<S, T> function) {
		Assert.notNull(list);
		Assert.notNull(function);
		return transform(list, new ArrayList<T>(list.size()), function, true);
	}

	public static <S, T> List<T> transform(List<? extends S> list, Function<S, T> function,
			boolean includeNullReferences) {
		Assert.notNull(list);
		Assert.notNull(function);
		return transform(list, new ArrayList<T>(list.size()), function, includeNullReferences);
	}

	public static <S, T> Set<T> transform(Set<? extends S> set, Function<S, T> function) {
		Assert.notNull(set);
		Assert.notNull(function);
		return transform(set, new HashSet<T>(set.size()), function, true);
	}

	public static <S, T> Set<T> transform(Set<? extends S> set, Function<S, T> function, boolean includeNullReferences) {
		Assert.notNull(set);
		Assert.notNull(function);
		return transform(set, new HashSet<T>(set.size()), function, includeNullReferences);
	}

}
