package de.airsupply.commons.core.util;

import java.util.HashSet;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ValidationUtils {

	private static final Logger LOGGER = LoggerFactory.getLogger(ValidationUtils.class);

	public static <T> void validate(Validator validator, T object, Class<?>... groups) {
		Set<ConstraintViolation<T>> results = validator.validate(object, groups);
		if (!results.isEmpty()) {
			Set<ConstraintViolation<?>> violations = new HashSet<>();
			for (ConstraintViolation<T> violation : results) {
				violations.add(violation);
				LOGGER.error(violation.toString());
			}
			throw new ConstraintViolationException(violations);
		}
	}

}
