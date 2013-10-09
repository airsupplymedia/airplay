package de.airsupply.commons.core.neo4j;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.ValidationException;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.springframework.data.neo4j.repository.GraphRepository;
import org.springframework.data.neo4j.support.Neo4jTemplate;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import de.airsupply.airplay.core.model.PersistentNode;
import de.airsupply.commons.core.neo4j.annotation.Unique;
import de.airsupply.commons.core.util.CollectionUtils;
import de.airsupply.commons.core.util.CollectionUtils.Function;
import de.airsupply.commons.core.util.Functions;

class UniquenessEvaluator<T> {

	protected static class Parameter {

		private final Object entity;

		private Class<?> fieldType;

		private Object fieldValue;

		private boolean fieldValueEvaluated;

		private final String name;

		protected Parameter(Object entity, String name) {
			this.entity = entity;
			this.name = name;
		}

		protected Parameter(Object entity, String name, Object fieldValue) {
			this.entity = entity;
			this.name = name;
			this.fieldValue = fieldValue;
			this.fieldValueEvaluated = true;
		}

		protected String getFieldName() {
			if (isIndexQuery()) {
				return name.substring(1, name.length());
			} else {
				return name;
			}
		}

		protected Object getFieldValue(Neo4jTemplate neo4jTemplate) {
			if (!fieldValueEvaluated) {
				Field field = ReflectionUtils.findField(entity.getClass(), getFieldName());
				Assert.notNull(field);
				field.setAccessible(true);
				try {
					fieldType = field.getType();
					fieldValue = field.get(entity);
					if (fieldValue != null && QueryUtils.isPersistent(neo4jTemplate, fieldValue)) {
						fieldValue = QueryUtils.getPersistentState(neo4jTemplate, fieldValue);
					}
				} catch (IllegalArgumentException | IllegalAccessException cause) {
					throw new ValidationException(cause);
				} finally {
					fieldValueEvaluated = true;
				}
			}
			return fieldValue;
		}

		protected TermQuery getFieldValueAsQuery(Neo4jTemplate neo4jTemplate) {
			if (!isIndexQuery() || StringUtils.isEmpty(getFieldValue(neo4jTemplate))) {
				return null;
			}
			String fieldValueString = getFieldValue(neo4jTemplate).toString();
			if (StringUtils.containsWhitespace(fieldValueString)) {
				return new TermQuery(new Term(getFieldName(), fieldValueString));
			} else {
				return new TermQuery(new Term(getFieldName(), fieldValueString));
			}
		}

		protected String getFieldValueAsQueryString(Neo4jTemplate neo4jTemplate) {
			TermQuery query = getFieldValueAsQuery(neo4jTemplate);
			return QueryUtils.buildIndexQuery(query.getTerm().field(), query.getTerm().text());
		}

		protected String getName() {
			return name;
		}

		protected String getParameterName() {
			if (isIndexQuery()) {
				return INDEX_QUERY_PREFIX + getFieldName();
			} else {
				return name;
			}
		}

		protected boolean isIndexQuery() {
			return name.startsWith(INDEX_QUERY_INITIALIZER);
		}

		protected boolean isValid(Neo4jTemplate neo4jTemplate) {
			Object fieldValue = getFieldValue(neo4jTemplate);
			if (fieldValue != null) {
				if (isIndexQuery()) {
					return String.class.equals(fieldType) && !StringUtils.isEmpty(fieldValue);
				}
				if (QueryUtils.isPersistable(neo4jTemplate, fieldValue)) {
					return QueryUtils.isPersistent(neo4jTemplate, fieldValue);
				}
				return true;
			}
			return false;
		}

	}

	private static final String INDEX_QUERY_INITIALIZER = ":";

	private static final String INDEX_QUERY_PREFIX = "query_";

	protected static Unique readAnnotation(Object object) {
		Assert.notNull(object);
		Unique unique = object.getClass().getAnnotation(Unique.class);
		Assert.notNull(unique);
		return unique;
	}

	private final String[] arguments;

	private Neo4jTemplate neo4jTemplate;

	private String query;

	private final T value;

	protected UniquenessEvaluator(T value, Neo4jTemplate neo4jTemplate) {
		this(value, neo4jTemplate, readAnnotation(value));
	}

	protected UniquenessEvaluator(T value, Neo4jTemplate neo4jTemplate, String query, String[] arguments) {
		this.value = value;
		this.neo4jTemplate = neo4jTemplate;
		this.query = query;
		this.arguments = arguments;
		Assert.notEmpty(arguments);
		Assert.noNullElements(arguments);
	}

	protected UniquenessEvaluator(T value, Neo4jTemplate neo4jTemplate, Unique unique) {
		this(value, neo4jTemplate, unique.query(), unique.arguments());
	}

	protected Map<String, Object> asParameterMap(Collection<Parameter> parameters) {
		Map<String, Object> parameterMap = new HashMap<>(parameters.size());
		for (Parameter parameter : parameters) {
			Object fieldValue;
			if (parameter.isIndexQuery()) {
				fieldValue = parameter.getFieldValueAsQueryString(neo4jTemplate);
			} else {
				fieldValue = parameter.getFieldValue(neo4jTemplate);
			}
			parameterMap.put(parameter.getParameterName(), fieldValue);
		}
		return parameterMap;
	}

	public boolean exists() {
		return QueryUtils.isPersistent(neo4jTemplate, value) || getExisting() != null;
	}

	protected List<?> findExisting() {
		boolean useQuery = StringUtils.hasText(query);
		List<?> result;
		List<Parameter> parameters = new ArrayList<>(arguments.length);
		if (useQuery) {
			parameters.add(new Parameter(value, "this", value));
		}
		boolean isArgumentsValid = true;
		for (int i = 0; i < arguments.length && isArgumentsValid; i++) {
			Parameter parameter = new Parameter(value, arguments[i]);
			if (parameter.isIndexQuery()) {
				query = query.replace(parameter.getName(), parameter.getParameterName());
			}
			if (!parameter.isValid(neo4jTemplate)) {
				return Collections.emptyList();
			}
			parameters.add(parameter);
		}
		if (useQuery) {
			result = runQuery(asParameterMap(parameters));
		} else {
			result = runPropertyAccess(parameters.get(0));
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	public T getExisting() {
		if (QueryUtils.isPersistent(neo4jTemplate, value)) {
			return value;
		} else {
			List<?> result = findExisting();
			Assert.isTrue(result.size() == 0 || result.size() == 1);
			if (result.size() == 1) {
				Assert.isInstanceOf(value.getClass(), result.get(0));
				return (T) result.get(0);
			} else {
				return null;
			}
		}
	}

	public boolean isUnique() {
		if (QueryUtils.isPersistent(neo4jTemplate, value)) {
			List<?> result = findExisting();
			if (result.size() == 0 || (result.size() == 1 && result.contains(value))) {
				return true;
			} else {
				return false;
			}
		} else {
			return !exists();
		}
	}

	protected List<?> runPropertyAccess(Parameter parameter) {
		GraphRepository<? extends Object> repository = neo4jTemplate.repositoryFor(value.getClass());
		String fieldName = parameter.getFieldName();
		Object fieldValue = parameter.getFieldValue(neo4jTemplate);
		Query fieldValueAsQuery = new TermQuery(new Term(fieldName, fieldValue.toString()));

		if (parameter.isIndexQuery()) {
			return CollectionUtils.asList(repository.findAllByQuery(fieldName, fieldValueAsQuery));
		} else {
			return CollectionUtils.asList(repository.findAllByPropertyValue(fieldName, fieldValue));
		}
	}

	protected List<?> runQuery(Map<String, Object> parameterMap) {
		Function<Object, PersistentNode> function = Functions.toPersistentState(neo4jTemplate, value.getClass());
		return CollectionUtils.transform(neo4jTemplate.query(query, parameterMap), function);
	}
}