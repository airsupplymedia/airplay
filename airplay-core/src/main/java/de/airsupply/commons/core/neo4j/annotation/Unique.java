package de.airsupply.commons.core.neo4j.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Collections;
import java.util.Map;

import javax.validation.Constraint;
import javax.validation.Payload;

import org.neo4j.graphdb.Node;

import de.airsupply.commons.core.neo4j.UniqueConstraintValidator;

@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = UniqueConstraintValidator.class)
public @interface Unique {

	public static class EmptyUniquenessTraverser implements UniquenessTraverser {

		@Override
		public Iterable<Node> traverse(Map<String, Object> parameters) {
			return Collections.emptyList();
		}

	}

	public interface UniquenessTraverser {

		Iterable<Node> traverse(Map<String, Object> parameters);

	}

	Class<?>[] groups() default {};

	String message() default "The given property is marked as unique!";

	String[] parameters();

	Class<? extends Payload>[] payload() default {};

	String query() default "";

	Class<? extends UniquenessTraverser> traverser() default EmptyUniquenessTraverser.class;

}
