package de.airsupply.commons.core.neo4j.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

import de.airsupply.commons.core.neo4j.UniqueConstraintValidator;

@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = UniqueConstraintValidator.class)
public @interface Unique {

	String[] arguments();

	Class<?>[] groups() default {};

	String message() default "The given property is marked as unique!";

	Class<? extends Payload>[] payload() default {};

	String query() default "";

}
