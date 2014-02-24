package de.airsupply.commons.core.neo4j;

import java.util.Objects;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.queryParser.QueryParser;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.NotFoundException;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.graphdb.traversal.Evaluation;
import org.neo4j.graphdb.traversal.Evaluator;
import org.springframework.beans.BeanUtils;
import org.springframework.data.neo4j.mapping.Neo4jPersistentEntity;
import org.springframework.data.neo4j.support.Neo4jTemplate;
import org.springframework.data.neo4j.support.mapping.StoredEntityType;
import org.springframework.util.Assert;

public abstract class QueryUtils {

	private static class DeletedNodeExcludingEvaluator implements Evaluator {

		@Override
		public Evaluation evaluate(Path path) {
			try {
				path.endNode().getId();
			} catch (IllegalStateException | NotFoundException exception) {
				return Evaluation.EXCLUDE_AND_CONTINUE;
			}
			return Evaluation.INCLUDE_AND_CONTINUE;
		}

	}

	private static class SystemNodeExcludingEvaluator implements Evaluator {

		@Override
		public Evaluation evaluate(Path path) {
			if (QueryUtils.isSystemNode(path.endNode())) {
				return Evaluation.EXCLUDE_AND_CONTINUE;
			}
			return Evaluation.INCLUDE_AND_CONTINUE;
		}

	}

	private static final String INDEX_QUERY = ":";

	private static final String INDEX_QUERY_QUOTE = "\"";

	public static String buildDefaultQuery(String query) {
		return buildDefaultQuery(query, null);
	}

	public static String buildDefaultQuery(String query, String fieldName) {
		String[] queryTokens = StringUtils.split(query);
		StringBuilder stringBuilder = new StringBuilder(query.length() + queryTokens.length * 2);
		for (int i = 0; i < queryTokens.length; i++) {
			if (fieldName != null) {
				stringBuilder.append(fieldName);
				stringBuilder.append(INDEX_QUERY);
			}
			stringBuilder.append(QueryParser.escape(queryTokens[i]));
			stringBuilder.append("*");
			if (i < queryTokens.length - 1) {
				stringBuilder.append(" AND ");
			}
		}
		return stringBuilder.toString();
	}

	public static String buildIndexQuery(String fieldName, String fieldValue) {
		return fieldName + INDEX_QUERY + INDEX_QUERY_QUOTE + fieldValue + INDEX_QUERY_QUOTE;
	}

	public static boolean exists(Neo4jTemplate neo4jTemplate, Object object) {
		Assert.isTrue(!isPersistent(neo4jTemplate, object));
		return new UniquenessEvaluator<>(object, neo4jTemplate).exists();
	}

	public static Evaluator getDeletedNodeExcludingEvaluator() {
		return new DeletedNodeExcludingEvaluator();
	}

	public static <T> T getExisting(Neo4jTemplate neo4jTemplate, T object) {
		Assert.isTrue(!isPersistent(neo4jTemplate, object));
		return new UniquenessEvaluator<>(object, neo4jTemplate).getExisting();
	}

	public static PropertyContainer getPersistentState(Neo4jTemplate neo4jTemplate, Object value) {
		return neo4jTemplate.getEntityStateHandler().getPersistentState(value);
	}

	public static SystemNodeExcludingEvaluator getSystemNodeExcludingEvaluator() {
		return new SystemNodeExcludingEvaluator();
	}

	public static boolean isPersistable(Neo4jTemplate neo4jTemplate, Object object) {
		Class<?> type = object.getClass();
		return !BeanUtils.isSimpleValueType(type)
				&& (neo4jTemplate.isNodeEntity(type) || neo4jTemplate.isRelationshipEntity(type));
	}

	public static boolean isPersistent(Neo4jTemplate neo4jTemplate, Object object) {
		if (!isPersistable(neo4jTemplate, object)) {
			return false;
		}
		StoredEntityType storedEntityType = neo4jTemplate.getEntityType(object.getClass());
		if (storedEntityType == null) {
			return false;
		}
		Neo4jPersistentEntity<?> entity = storedEntityType.getEntity();
		if (entity == null) {
			return false;
		}
		Object id = entity.getPersistentId(object);
		if (id == null) {
			return false;
		}
		Node nodeById = neo4jTemplate.getGraphDatabase().getNodeById(((Long) id).longValue());
		if (nodeById == null) {
			return false;
		}
		try {
			nodeById.getPropertyKeys();
		} catch (IllegalStateException | NotFoundException notFoundException) {
			return false;
		}
		return true;
	}

	public static boolean isSystemNode(Node node) {
		Assert.notNull(node);
		return Boolean.valueOf(Objects.toString(node.getProperty("system"))).booleanValue();
	}

	public static boolean isTransient(Neo4jTemplate neo4jTemplate, Object object) {
		return isPersistable(neo4jTemplate, object) && !isPersistent(neo4jTemplate, object);
	}

}
