package de.airsupply.commons.core.neo4j.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

import de.airsupply.commons.core.neo4j.PersistentConstraintValidator;

@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = PersistentConstraintValidator.class)
public @interface Persistent {

	Class<?>[] groups() default {};

	String message() default "The given property is marked as persistent!";

	Class<? extends Payload>[] payload() default {};

}
