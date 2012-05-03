package de.airsupply.commons.core.util;

import java.util.HashSet;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class ValidationUtils {

	private static final Log LOG = LogFactory.getLog(ValidationUtils.class);

	public static <T extends Object> void validate(Validator validator, T object, Class<?>... groups) {
		Set<ConstraintViolation<T>> results = validator.validate(object, groups);
		if (!results.isEmpty()) {
			Set<ConstraintViolation<?>> violations = new HashSet<>();
			for (ConstraintViolation<T> violation : results) {
				violations.add(violation);
				LOG.error(violation);
			}
			throw new ConstraintViolationException(violations);
		}
	}

}
