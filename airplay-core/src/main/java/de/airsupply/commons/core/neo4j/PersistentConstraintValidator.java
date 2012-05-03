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

	private boolean isPersistentNeo4jObject(Object fieldValue) {
		return (neo4jTemplate.isNodeEntity(fieldValue.getClass()) || neo4jTemplate.isRelationshipEntity(fieldValue
				.getClass())) && neo4jTemplate.getEntityStateHandler().hasPersistentState(fieldValue);
	}

	@Override
	public boolean isValid(Object value, ConstraintValidatorContext context) {
		return value == null || isPersistentNeo4jObject(value);
	}

}
