package de.airsupply.commons.core.neo4j;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.support.Neo4jTemplate;
import org.springframework.util.Assert;

import de.airsupply.commons.core.neo4j.annotation.Unique;

public class UniqueConstraintValidator implements ConstraintValidator<Unique, Object> {

	private String[] arguments;

	@Autowired
	private Neo4jTemplate neo4jTemplate;

	private String query;

	@Override
	public void initialize(Unique constraintAnnotation) {
		query = constraintAnnotation.query();
		arguments = constraintAnnotation.arguments();
		Assert.notEmpty(arguments);
		Assert.noNullElements(arguments);
	}

	@Override
	public boolean isValid(Object value, ConstraintValidatorContext context) {
		UniquenessEvaluator<Object> evaluator = new UniquenessEvaluator<>(value, neo4jTemplate, query, arguments);
		return evaluator.isUnique();
	}

}
