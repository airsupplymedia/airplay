package de.airsupply.commons.core.neo4j;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.validation.ConstraintViolationException;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.traversal.Evaluation;
import org.neo4j.graphdb.traversal.Evaluators;
import org.neo4j.graphdb.traversal.Traverser;
import org.neo4j.kernel.Traversal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.repository.GraphRepository;
import org.springframework.data.neo4j.support.Neo4jTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.ReflectionUtils.FieldCallback;

import de.airsupply.commons.core.util.CollectionUtils;
import de.airsupply.commons.core.util.Functions;
import de.airsupply.commons.core.util.ValidationUtils;

public abstract class Neo4jServiceSupport {

	public static interface DeleteAction {
	}

	@Autowired
	private Neo4jTemplate neo4jTemplate;

	@Transactional
	public <T> void delete(Iterable<T> objects) {
		Assert.notNull(objects);

		List<Node> nodes = CollectionUtils.transform(objects, Functions.toNode(neo4jTemplate));
		if (getReferencers(nodes.toArray(new Node[nodes.size()])).iterator().hasNext()) {
			throw new ConstraintViolationException("The objects still have referencers and may not be deleted!", null);
		}
		for (T object : objects) {
			neo4jTemplate.delete(object);
		}
	}

	@Transactional
	public <T> void delete(T object) {
		Assert.notNull(object);

		Node node = neo4jTemplate.getPersistentState(object);
		if (getReferencers(node).iterator().hasNext()) {
			throw new ConstraintViolationException("The object still has referencers and may not be deleted!", null);
		}
		neo4jTemplate.delete(object);
	}

	public <T> boolean exists(T object) {
		return find(object) != null;
	}

	public <T> T fetch(T object) {
		return neo4jTemplate.fetch(object);
	}

	public <T> List<T> find(Class<T> entityClass) {
		return CollectionUtils.asList(neo4jTemplate.findAll(entityClass));
	}

	public <T> T find(Long identifier, Class<T> entityClass) {
		return neo4jTemplate.findOne(identifier.longValue(), entityClass);
	}

	public <T> T find(T object) {
		return QueryUtils.getExisting(neo4jTemplate, object);
	}

	public <T> List<T> findOrCreate(List<T> objects) {
		return findOrCreate(objects, false);
	}

	public <T> List<T> findOrCreate(List<T> objects, boolean recursively) {
		Assert.notNull(objects);
		List<T> result = new ArrayList<>(objects.size());
		for (T t : objects) {
			result.add(findOrCreate(t, recursively));
		}
		return Collections.unmodifiableList(result);
	}

	public <T> Set<T> findOrCreate(Set<T> objects) {
		return findOrCreate(objects, false);
	}

	public <T> Set<T> findOrCreate(Set<T> objects, boolean recursively) {
		Assert.notNull(objects);
		Set<T> result = new HashSet<>(objects.size());
		for (T t : objects) {
			result.add(findOrCreate(t, recursively));
		}
		return Collections.unmodifiableSet(result);
	}

	public <T> T findOrCreate(T object) {
		return findOrCreate(object, false);
	}

	@SuppressWarnings("unchecked")
	public <T> T findOrCreate(T object, boolean recursively) {
		Assert.notNull(object);
		if (recursively) {
			return (T) findOrCreateRecursively(object, new HashMap<Object, Object>());
		} else {
			T result = find(object);
			if (result == null) {
				result = save(object);
			}
			return result;
		}
	}

	private Object findOrCreateRecursively(final Object object, final Map<Object, Object> processed) {
		if (!QueryUtils.isPersistable(neo4jTemplate, object)) {
			return object;
		}
		if (QueryUtils.isPersistent(neo4jTemplate, object)) {
			return object;
		}
		if (processed.containsKey(object)) {
			Object value = processed.get(object);
			if (value != null) {
				return value;
			} else {
				return object;
			}
		}

		processed.put(object, null);
		ReflectionUtils.doWithFields(object.getClass(), new FieldCallback() {

			@Override
			public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {
				field.setAccessible(true);
				Object value = field.get(object);
				if (value != null) {
					if (value instanceof Set) {
						Set<?> oldSet = (Set<?>) value;
						Set<Object> newSet = new HashSet<>(oldSet.size());
						for (Object content : oldSet) {
							if (QueryUtils.isPersistable(neo4jTemplate, content)) {
								newSet.add(findOrCreateRecursively(content, processed));
							}
						}
						field.set(object, newSet);
					} else if (QueryUtils.isPersistable(neo4jTemplate, value)) {
						field.set(object, findOrCreateRecursively(value, processed));
					}
				}
			}

		});

		Object result = find(object);
		if (result == null) {
			result = save(object);
		}
		processed.put(object, result);
		return result;
	}

	public Neo4jTemplate getNeo4jTemplate() {
		return neo4jTemplate;
	}

	private Iterable<Node> getReferencers(Node node) {
		// @formatter:off
		Traverser traverser = Traversal
			.description()
			.expand(Traversal.expanderForAllTypes(Direction.INCOMING))
			.evaluator(Evaluators.excludeStartPosition())
			.evaluator(QueryUtils.getSystemNodeExcludingEvaluator())
			.evaluator(QueryUtils.getDeletedNodeExcludingEvaluator())
			.evaluator(Evaluators.toDepth(1))
			.traverse(node);
		// @formatter:on
		return traverser.nodes();
	}

	private Iterable<Node> getReferencers(Node... nodes) {
		// @formatter:off
		Traverser traverser = Traversal
				.description()
				.expand(Traversal.expanderForAllTypes(Direction.INCOMING))
				.evaluator(Evaluators.excludeStartPosition())
				.evaluator(QueryUtils.getSystemNodeExcludingEvaluator())
				.evaluator(QueryUtils.getDeletedNodeExcludingEvaluator())
				.evaluator(Evaluators.toDepth(1))
				.evaluator(Evaluators.endNodeIs(Evaluation.EXCLUDE_AND_CONTINUE, Evaluation.INCLUDE_AND_CONTINUE, nodes))
				.traverse(nodes);
		// @formatter:on
		return traverser.nodes();
	}

	@Transactional
	public <T> T save(T object) {
		Assert.notNull(object);
		ValidationUtils.validate(neo4jTemplate.getValidator(), object);

		@SuppressWarnings("unchecked")
		GraphRepository<T> repository = (GraphRepository<T>) neo4jTemplate.repositoryFor(object.getClass());
		return repository.save(object);
	}

}
