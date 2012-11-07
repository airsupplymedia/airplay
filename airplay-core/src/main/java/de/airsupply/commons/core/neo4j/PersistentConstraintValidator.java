package de.airsupply.commons.core.neo4j;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.support.Neo4jTemplate;

import de.airsupply.commons.core.neo4j.annotation.Persistent;

public class PersistentConstraintValidator implements ConstraintValidator<Persistent, Object> {

	@Autowired
	private Neo4jTemplate neo4jTemplate;

	@Override
	public void initialize(Persistent constraintAnnotation) {
	}

	@Override
	public boolean isValid(Object value, ConstraintValidatorContext context) {
		if (value == null) {
			return true;
		}
		if (value instanceof Iterable) {
			Iterable<?> iterable = (Iterable<?>) value;
			for (Object object : iterable) {
				if (!QueryUtils.isPersistent(neo4jTemplate, object)) {
					return false;
				}
			}
			return true;
		} else if (QueryUtils.isPersistent(neo4jTemplate, value)) {
			return true;
		}
		return false;
	}

}
