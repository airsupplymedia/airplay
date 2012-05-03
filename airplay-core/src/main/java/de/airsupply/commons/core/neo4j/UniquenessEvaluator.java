package de.airsupply.commons.core.neo4j;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.validation.ValidationException;

import org.neo4j.graphdb.PropertyContainer;
import org.springframework.data.neo4j.conversion.Result;
import org.springframework.data.neo4j.mapping.MappingPolicy;
import org.springframework.data.neo4j.repository.GraphRepository;
import org.springframework.data.neo4j.support.Neo4jTemplate;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import de.airsupply.commons.core.neo4j.annotation.Unique;
import de.airsupply.commons.core.util.CollectionUtils;

class UniquenessEvaluator<T extends Object> {

	private static final String INDEX_QUERY_INITIALIZER = ":";

	private static Unique readAnnotation(Object object) {
		Assert.notNull(object);
		Unique unique = object.getClass().getAnnotation(Unique.class);
		Assert.notNull(unique);
		return unique;
	}

	private final String[] arguments;

	private Neo4jTemplate neo4jTemplate;

	private final String query;

	private boolean useQuery = true;

	private final T value;

	UniquenessEvaluator(T value, Neo4jTemplate neo4jTemplate) {
		this(value, neo4jTemplate, readAnnotation(value));
	}

	UniquenessEvaluator(T value, Neo4jTemplate neo4jTemplate, String query, String[] arguments) {
		this.value = value;
		this.neo4jTemplate = neo4jTemplate;
		this.query = query;
		this.arguments = arguments;
		Assert.notEmpty(arguments);
		Assert.noNullElements(arguments);
		if (!StringUtils.hasText(query)) {
			Assert.isTrue(arguments.length == 1);
			useQuery = false;
		}
	}

	UniquenessEvaluator(T value, Neo4jTemplate neo4jTemplate, Unique unique) {
		this(value, neo4jTemplate, unique.query(), unique.arguments());
	}

	public boolean exists() {
		return QueryUtils.isPersistent(neo4jTemplate, value) || getExisting() != null;
	}

	@SuppressWarnings("unchecked")
	public T getExisting() {
		if (QueryUtils.isPersistent(neo4jTemplate, value)) {
			return value;
		} else {
			List<? extends Object> result;
			if (useQuery) {
				result = runQuery();
			} else {
				result = runPropertyAccess();
			}
			Assert.isTrue(result.size() == 0 || result.size() == 1);
			if (result.size() == 1) {
				Assert.isInstanceOf(value.getClass(), result.get(0));
				return (T) result.get(0);
			} else {
				return null;
			}
		}
	}

	private boolean handleArgument(Object value, HashMap<String, Object> argumentMap, String fieldName) {
		boolean isValid = true;
		boolean isIndexQuery = fieldName.startsWith(INDEX_QUERY_INITIALIZER);
		if (isIndexQuery) {
			Assert.isTrue(useQuery);
			fieldName = fieldName.substring(1, fieldName.length());
		}
		Field field = ReflectionUtils.findField(value.getClass(), fieldName);
		Assert.notNull(field);
		field.setAccessible(true);
		try {
			Object fieldValue = field.get(value);
			if (fieldValue != null) {
				if (QueryUtils.isPersistent(neo4jTemplate, fieldValue)) {
					Assert.isTrue(useQuery);
					fieldValue = QueryUtils.getPersistentState(neo4jTemplate, fieldValue);
				}
				if (isIndexQuery) {
					Assert.isTrue(useQuery);
					fieldValue = QueryUtils.buildIndexQuery(fieldName, fieldValue);
				}
				argumentMap.put(fieldName, fieldValue);
			} else {
				isValid = false;
			}
		} catch (IllegalArgumentException cause) {
			throw new ValidationException(cause);
		} catch (IllegalAccessException cause) {
			throw new ValidationException(cause);
		}
		return isValid;
	}

	public boolean isUnique() {
		if (QueryUtils.isPersistent(neo4jTemplate, value)) {
			List<? extends Object> result;
			if (useQuery) {
				result = runQuery();
			} else {
				result = runPropertyAccess();
			}
			if (result.size() == 1 && result.contains(value)) {
				return true;
			} else {
				return false;
			}
		} else {
			return !exists();
		}
	}

	private List<? extends Object> runPropertyAccess() {
		HashMap<String, Object> argumentMap = new HashMap<>();
		boolean isArgumentValid = handleArgument(value, argumentMap, arguments[0]);
		if (isArgumentValid) {
			GraphRepository<? extends Object> repository = neo4jTemplate.repositoryFor(value.getClass());
			List<? extends Object> result = CollectionUtils.asList(repository.findAllByPropertyValue(arguments[0],
					argumentMap.get(arguments[0])));
			return result;
		}
		return Collections.emptyList();
	}

	private List<? extends Object> runQuery() {
		HashMap<String, Object> argumentMap = new HashMap<>();
		argumentMap.put("this", value);

		boolean isArgumentsValid = true;
		for (int i = 0; i < arguments.length && isArgumentsValid; i++) {
			isArgumentsValid = handleArgument(value, argumentMap, arguments[i]);
		}
		if (isArgumentsValid) {
			Result<Map<String, Object>> result = neo4jTemplate.query(query, argumentMap);
			List<Object> list = new ArrayList<>();
			Iterator<Map<String, Object>> iterator = result.iterator();
			while (iterator.hasNext()) {
				Map<String, Object> next = iterator.next();
				Collection<Object> values = next.values();
				for (Object object : values) {
					MappingPolicy mappingPolicy = neo4jTemplate.getMappingPolicy(value.getClass());
					PropertyContainer propertyContainer = (PropertyContainer) object;
					list.add(neo4jTemplate.createEntityFromState(propertyContainer, value.getClass(), mappingPolicy));
				}
			}
			return list;
		}
		return Collections.emptyList();
	}

}