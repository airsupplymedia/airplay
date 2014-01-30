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
import de.airsupply.commons.core.neo4j.annotation.Unique.UniquenessTraverser;
import de.airsupply.commons.core.neo4j.annotation.Unique.UniquenessTraverserFactory;
import de.airsupply.commons.core.util.CollectionUtils;
import de.airsupply.commons.core.util.Functions;

class UniquenessEvaluator<T> {

	private static enum EvaluationType {

		PARAMETER(false), QUERY(true), TRAVERSER(true);

		private final boolean requiresThisParameter;

		private EvaluationType(boolean requiresThisParameter) {
			this.requiresThisParameter = requiresThisParameter;
		}

		public boolean isRequiresThisParameter() {
			return requiresThisParameter;
		}

	}

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

	private Neo4jTemplate neo4jTemplate;

	private final String[] parameterNames;

	private String query;

	private final Class<? extends UniquenessTraverserFactory> traverserFactoryClass;

	private final T value;

	protected UniquenessEvaluator(T value, Neo4jTemplate neo4jTemplate) {
		this(value, neo4jTemplate, readAnnotation(value));
	}

	protected UniquenessEvaluator(T value, Neo4jTemplate neo4jTemplate, String query, String[] parameterNames,
			Class<? extends UniquenessTraverserFactory> traverserFactoryClass) {
		this.value = value;
		this.neo4jTemplate = neo4jTemplate;
		this.query = query;
		this.parameterNames = parameterNames;
		this.traverserFactoryClass = traverserFactoryClass;
		Assert.notEmpty(parameterNames);
		Assert.noNullElements(parameterNames);
	}

	protected UniquenessEvaluator(T value, Neo4jTemplate neo4jTemplate, Unique unique) {
		this(value, neo4jTemplate, unique.query(), unique.parameters(), unique.traverser());
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

	protected EvaluationType computeEvaluationType() {
		if (StringUtils.hasText(query)) {
			return EvaluationType.QUERY;
		} else if (!UniquenessTraverserFactory.class.equals(traverserFactoryClass)) {
			return EvaluationType.TRAVERSER;
		} else if (parameterNames.length > 0) {
			return EvaluationType.PARAMETER;
		} else {
			throw new IllegalArgumentException("Evaluation type could not be determined!");
		}
	}

	public boolean exists() {
		return QueryUtils.isPersistent(neo4jTemplate, value) || getExisting() != null;
	}

	protected List<?> findExisting() {
		EvaluationType evaluationType = computeEvaluationType();
		List<Parameter> parameters = new ArrayList<>(parameterNames.length);
		if (evaluationType.isRequiresThisParameter()) {
			parameters.add(new Parameter(value, "this", value));
		}
		boolean isArgumentsValid = true;
		for (int i = 0; i < parameterNames.length && isArgumentsValid; i++) {
			Parameter parameter = new Parameter(value, parameterNames[i]);
			if (parameter.isIndexQuery()) {
				query = query.replace(parameter.getName(), parameter.getParameterName());
			}
			if (!parameter.isValid(neo4jTemplate)) {
				return Collections.emptyList();
			}
			parameters.add(parameter);
		}
		switch (evaluationType) {
		case QUERY:
			return runQuery(asParameterMap(parameters));
		case TRAVERSER:
			return runTraverser(asParameterMap(parameters));
		default:
			return runPropertyAccess(parameters.get(0));
		}
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

	protected List<?> runQuery(Map<String, Object> parameters) {
		return CollectionUtils.transform(neo4jTemplate.query(query, parameters),
				Functions.<PersistentNode> toPersistentState(neo4jTemplate, value.getClass()));
	}

	protected List<?> runTraverser(Map<String, Object> parameters) {
		UniquenessTraverserFactory traverserFactory;
		try {
			traverserFactory = traverserFactoryClass.newInstance();
		} catch (InstantiationException | IllegalAccessException | SecurityException exception) {
			throw new IllegalArgumentException("Traverser could not be instantiated!", exception);
		}
		UniquenessTraverser traverser = traverserFactory.create(parameters);
		Assert.notNull(traverser);
		return CollectionUtils.transform(traverser.traverse(parameters),
				Functions.<PersistentNode> toPersistentState(neo4jTemplate, value.getClass()));
	}

}