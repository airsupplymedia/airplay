package de.airsupply.commons.core.neo4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.repository.GraphRepository;
import org.springframework.data.neo4j.support.Neo4jTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import de.airsupply.commons.core.util.ValidationUtils;

public abstract class Neo4jServiceSupport {

	@Autowired
	private Neo4jTemplate neo4jTemplate;

	public <T extends Object> boolean exists(T object) {
		return find(object) != null;
	}

	public <T extends Object> T fetch(T object) {
		return neo4jTemplate.fetch(object);
	}

	public <T extends Object> T find(T object) {
		return QueryUtils.getExisting(neo4jTemplate, object);
	}

	public <T extends Object> List<T> findOrCreate(List<T> objects) {
		Assert.notNull(objects);
		List<T> result = new ArrayList<>(objects.size());
		for (T t : objects) {
			result.add(findOrCreate(t));
		}
		return Collections.unmodifiableList(result);
	}

	public <T extends Object> T findOrCreate(T object) {
		Assert.notNull(object);
		T result = find(object);
		if (result == null) {
			result = save(object);
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	@Transactional
	public <T extends Object> T save(T object) {
		Assert.notNull(object);
		ValidationUtils.validate(neo4jTemplate.getValidator(), object);
		GraphRepository<T> repository = (GraphRepository<T>) neo4jTemplate.repositoryFor(object.getClass());
		return repository.save(object);
	}

}
