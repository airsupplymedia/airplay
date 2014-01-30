package de.airsupply.commons.core.neo4j;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.support.Neo4jTemplate;
import org.springframework.util.Assert;

import de.airsupply.commons.core.neo4j.annotation.Unique;
import de.airsupply.commons.core.neo4j.annotation.Unique.UniquenessTraverserFactory;

public class UniqueConstraintValidator implements ConstraintValidator<Unique, Object> {

	@Autowired
	private Neo4jTemplate neo4jTemplate;

	private String[] parameters;

	private String query;

	private Class<? extends UniquenessTraverserFactory> traverserFactoryClass;

	@Override
	public void initialize(Unique constraintAnnotation) {
		query = constraintAnnotation.query();
		traverserFactoryClass = constraintAnnotation.traverser();
		parameters = constraintAnnotation.parameters();
		Assert.notEmpty(parameters);
		Assert.noNullElements(parameters);
	}

	@Override
	public boolean isValid(Object value, ConstraintValidatorContext context) {
		return new UniquenessEvaluator<>(value, neo4jTemplate, query, parameters, traverserFactoryClass).isUnique();
	}

}
