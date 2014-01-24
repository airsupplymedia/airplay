package de.airsupply.commons.core.neo4j;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.helpers.Counter;
import org.neo4j.helpers.collection.MapUtil;
import org.neo4j.index.lucene.unsafe.batchinsert.LuceneBatchInserterIndexProvider;
import org.neo4j.unsafe.batchinsert.BatchInserter;
import org.neo4j.unsafe.batchinsert.BatchInserterIndex;
import org.neo4j.unsafe.batchinsert.BatchInserterIndexProvider;
import org.neo4j.unsafe.batchinsert.BatchInserters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mapping.Association;
import org.springframework.data.mapping.AssociationHandler;
import org.springframework.data.mapping.PropertyHandler;
import org.springframework.data.neo4j.mapping.IndexInfo;
import org.springframework.data.neo4j.mapping.MappingPolicy;
import org.springframework.data.neo4j.mapping.Neo4jPersistentEntity;
import org.springframework.data.neo4j.mapping.Neo4jPersistentProperty;
import org.springframework.data.neo4j.support.Neo4jTemplate;
import org.springframework.data.neo4j.support.index.IndexType;
import org.springframework.data.neo4j.support.typerepresentation.AbstractIndexingTypeRepresentationStrategy;
import org.springframework.data.neo4j.support.typerepresentation.IndexingNodeTypeRepresentationStrategy;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

@Component
public class Neo4jBatchInserter {

	private static class NodeWrapper {

		private final long id;

		private final String indexKey;

		private final List<PropertyWrapper> properties = new ArrayList<>();

		private NodeWrapper(long id, String indexKey) {
			this.id = id;
			this.indexKey = indexKey;
		}

		public long getId() {
			return id;
		}

		public String getIndexKey() {
			return indexKey;
		}

		public List<PropertyWrapper> getProperties() {
			return properties;
		}

		public Map<String, Object> getPropertiesAsMap() {
			Map<String, Object> propertiesAsMap = new HashMap<>();
			for (PropertyWrapper property : getProperties()) {
				propertiesAsMap.put(property.getKey(), property.getValue());
			}
			return propertiesAsMap;
		}

		public void put(IndexType indexType, String indexName, String key, Object value) {
			if (value == null) {
				return;
			}
			properties.add(new PropertyWrapper(indexType, indexName, key, value));
		}

	}

	private static class PropertyWrapper {

		private final String indexName;

		private final IndexType indexType;

		private final String key;

		private final Object value;

		public PropertyWrapper(IndexType indexType, String indexName, String key, Object value) {
			this.indexType = indexType;
			this.indexName = indexName;
			this.key = key;
			this.value = value;
		}

		public String getIndexName() {
			return indexName;
		}

		public IndexType getIndexType() {
			return indexType;
		}

		public String getKey() {
			return key;
		}

		public Object getValue() {
			return value;
		}

		public boolean isIndexed() {
			return indexName != null;
		}

	}

	private static class RelationshipWrapper {

		private final long endNode;

		private final RelationshipType relationshipType;

		private final long startNode;

		private RelationshipWrapper(long startNode, long endNode, RelationshipType relationshipType) {
			this.startNode = startNode;
			this.endNode = endNode;
			this.relationshipType = relationshipType;
		}

		public long getEndNode() {
			return endNode;
		}

		public RelationshipType getRelationshipType() {
			return relationshipType;
		}

		public long getStartNode() {
			return startNode;
		}

	}

	private static final String NODE_INDEX_NAME = IndexingNodeTypeRepresentationStrategy.INDEX_NAME;

	private static final String TYPE_PROPERTY_NAME = AbstractIndexingTypeRepresentationStrategy.TYPE_PROPERTY_NAME;

	private Counter counter = new Counter();

	@Autowired
	private Neo4jTemplate template;

	private Long computeAndSetId(final Object object, final Neo4jPersistentEntity<?> entity) {
		Long id = (Long) entity.getPersistentId(object);
		if (id == null) {
			counter.inc();
			id = Long.valueOf(counter.count());
			Field field = ReflectionUtils.findField(object.getClass(), entity.getIdProperty().getName());
			field.setAccessible(true);
			ReflectionUtils.setField(field, object, id);
		}
		return id;
	}

	private NodeWrapper createNode(final Object object) {
		if (template.isNodeEntity(object.getClass())) {
			final MappingPolicy mappingPolicy = template.getMappingPolicy(object.getClass());
			final Neo4jPersistentEntity<?> entity = template.getEntityType(object.getClass()).getEntity();
			final NodeWrapper node = new NodeWrapper(computeAndSetId(object, entity).longValue(), entity.getName());
			node.put(null, null, TYPE_PROPERTY_NAME, entity.getName());
			entity.doWithProperties(new PropertyHandler<Neo4jPersistentProperty>() {

				@Override
				public void doWithPersistentProperty(Neo4jPersistentProperty property) {
					if (property.isIdProperty() || property.isTransient()
							|| !property.isSerializablePropertyField(template.getConversionService())) {
						return;
					}
					IndexType indexType = null;
					String indexName = null;
					if (property.isIndexed()) {
						IndexInfo indexInfo = property.getIndexInfo();
						indexType = indexInfo.getIndexType();
						indexName = template.getIndex(property, object.getClass()).getName();
					}
					String key = property.getName();
					Object value = property.getValue(object, mappingPolicy);
					node.put(indexType, indexName, key, value);
				}

			});
			return node;
		}
		return null;
	}

	private List<NodeWrapper> createNodes(List<Object> objects) {
		List<NodeWrapper> nodes = new ArrayList<>();
		for (Object object : objects) {
			if (object != null) {
				nodes.add(createNode(fetch(object)));
			}
		}
		return nodes;
	}

	private RelationshipWrapper createRelationship(Object start, Object end, Neo4jPersistentEntity<?> entity,
			Neo4jPersistentProperty property) {
		long startNode = ((Long) entity.getPersistentId(start)).longValue();
		long endNode = ((Long) entity.getPersistentId(end)).longValue();
		RelationshipType relationshipType = property.getRelationshipInfo().getRelationshipType();
		if (property.getRelationshipInfo().getDirection() == Direction.INCOMING) {
			return new RelationshipWrapper(endNode, startNode, relationshipType);
		} else {
			return new RelationshipWrapper(startNode, endNode, relationshipType);
		}
	}

	private List<RelationshipWrapper> createRelationships(List<Object> objects) {
		List<RelationshipWrapper> relationships = new ArrayList<>();
		for (Object object : objects) {
			if (object != null) {
				relationships.addAll(createRelationships(fetch(object)));
			}
		}
		return relationships;
	}

	private List<RelationshipWrapper> createRelationships(final Object object) {
		if (template.isNodeEntity(object.getClass())) {
			final MappingPolicy mappingPolicy = template.getMappingPolicy(object.getClass());
			final Neo4jPersistentEntity<?> entity = template.getEntityType(object.getClass()).getEntity();

			final List<RelationshipWrapper> relationships = new ArrayList<>();
			entity.doWithAssociations(new AssociationHandler<Neo4jPersistentProperty>() {

				@Override
				public void doWithAssociation(Association<Neo4jPersistentProperty> association) {
					Neo4jPersistentProperty property = association.getInverse();
					Object target = property.getValue(object, mappingPolicy);
					if (target == null) {
						return;
					}
					if (property.getRelationshipInfo().isCollection()) {
						for (Object current : (Iterable<?>) target) {
							relationships.add(createRelationship(object, fetch(current), entity, property));
						}
					} else {
						relationships.add(createRelationship(object, fetch(target), entity, property));
					}
				}

			});
			return relationships;
		}
		return Collections.emptyList();
	}

	private Object fetch(Object object) {
		if (template.isManaged(object)) {
			return template.fetch(object);
		}
		return object;
	}

	public void runBatch(String storeDirectory, List<Object> objects) {
		BatchInserter inserter = BatchInserters.inserter(storeDirectory);
		BatchInserterIndexProvider indexProvider = new LuceneBatchInserterIndexProvider(inserter);
		BatchInserterIndex nodeTypeIndex = indexProvider.nodeIndex(NODE_INDEX_NAME, IndexType.SIMPLE.getConfig());
		try {
			for (NodeWrapper node : createNodes(objects)) {
				inserter.createNode(node.getId(), node.getPropertiesAsMap());
				for (PropertyWrapper property : node.getProperties()) {
					if (property.isIndexed()) {
						String indexName = property.getIndexName();
						Map<String, String> indexConfig = property.getIndexType().getConfig();
						BatchInserterIndex index = indexProvider.nodeIndex(indexName, indexConfig);
						index.add(node.getId(), MapUtil.map(property.getKey(), property.getValue()));
					}
				}
				nodeTypeIndex.add(node.getId(), MapUtil.map("className", node.getIndexKey()));
			}
			for (RelationshipWrapper relationship : createRelationships(objects)) {
				long startNode = relationship.getStartNode();
				long endNode = relationship.getEndNode();
				Map<String, Object> properties = Collections.<String, Object> emptyMap();
				inserter.createRelationship(startNode, endNode, relationship.getRelationshipType(), properties);
			}
		} catch (Exception exception) {
			exception.printStackTrace();
		} finally {
			counter = new Counter();
			nodeTypeIndex.flush();
			indexProvider.shutdown();
			inserter.shutdown();
		}
	}
}
