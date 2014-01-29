package de.airsupply.commons.core.neo4j;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.support.Neo4jTemplate;
import org.springframework.util.Assert;

import de.airsupply.commons.core.neo4j.annotation.Unique;
import de.airsupply.commons.core.neo4j.annotation.Unique.UniquenessTraverser;

public class UniqueConstraintValidator implements ConstraintValidator<Unique, Object> {

	private String[] parameters;

	@Autowired
	private Neo4jTemplate neo4jTemplate;

	private String query;

	private Class<? extends UniquenessTraverser> traverser;

	@Override
	public void initialize(Unique constraintAnnotation) {
		query = constraintAnnotation.query();
		traverser = constraintAnnotation.traverser();
		parameters = constraintAnnotation.parameters();
		Assert.notEmpty(parameters);
		Assert.noNullElements(parameters);
	}

	@Override
	public boolean isValid(Object value, ConstraintValidatorContext context) {
		UniquenessEvaluator<Object> evaluator = new UniquenessEvaluator<>(value, neo4jTemplate, query, parameters,
				traverser);
		return evaluator.isUnique();
	}

}
