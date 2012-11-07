package de.airsupply.commons.core.neo4j;

import org.apache.commons.lang.StringUtils;
import org.neo4j.graphdb.PropertyContainer;
import org.springframework.beans.BeanUtils;
import org.springframework.data.neo4j.support.Neo4jTemplate;
import org.springframework.util.Assert;

public abstract class QueryUtils {

	private static final String INDEX_QUERY = ":";

	private static final String INDEX_QUERY_QUOTE = "\"";

	public static String buildDefaultQuery(final String query) {
		String[] queryTokens = StringUtils.split(query);
		StringBuilder stringBuilder = new StringBuilder(query.length() + queryTokens.length * 2);
		for (int i = 0; i < queryTokens.length; i++) {
			stringBuilder.append(queryTokens[i]);
			stringBuilder.append("*");
			if (i < queryTokens.length - 1) {
				stringBuilder.append(" && ");
			}
		}
		return stringBuilder.toString();
	}

	public static String buildIndexQuery(String fieldName, Object fieldValue) {
		return fieldName + INDEX_QUERY + INDEX_QUERY_QUOTE + fieldValue + INDEX_QUERY_QUOTE;
	}

	public static boolean exists(Neo4jTemplate neo4jTemplate, Object object) {
		Assert.isTrue(!isPersistent(neo4jTemplate, object));
		return new UniquenessEvaluator<>(object, neo4jTemplate).exists();
	}

	public static <T> T getExisting(Neo4jTemplate neo4jTemplate, T object) {
		Assert.isTrue(!isPersistent(neo4jTemplate, object));
		return new UniquenessEvaluator<>(object, neo4jTemplate).getExisting();
	}

	public static PropertyContainer getPersistentState(Neo4jTemplate neo4jTemplate, Object value) {
		return neo4jTemplate.getEntityStateHandler().getPersistentState(value);
	}

	public static boolean isPersistable(Neo4jTemplate neo4jTemplate, Object object) {
		return !BeanUtils.isSimpleValueType(object.getClass())
				&& (neo4jTemplate.isNodeEntity(object.getClass()) || neo4jTemplate.isRelationshipEntity(object
						.getClass()));
	}

	public static boolean isPersistent(Neo4jTemplate neo4jTemplate, Object object) {
		return isPersistable(neo4jTemplate, object) && neo4jTemplate.getEntityStateHandler().hasPersistentState(object);
	}

	public static boolean isTransient(Neo4jTemplate neo4jTemplate, Object object) {
		return isPersistable(neo4jTemplate, object)
				&& !neo4jTemplate.getEntityStateHandler().hasPersistentState(object);
	}

}
