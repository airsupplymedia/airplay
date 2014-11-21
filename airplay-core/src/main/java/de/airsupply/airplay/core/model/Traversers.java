package de.airsupply.airplay.core.model;

import static de.airsupply.commons.core.util.CollectionUtils.transform;
import static org.neo4j.graphdb.Direction.BOTH;
import static org.neo4j.graphdb.Direction.INCOMING;
import static org.neo4j.graphdb.Direction.OUTGOING;
import static org.neo4j.graphdb.DynamicRelationshipType.withName;
import static org.neo4j.graphdb.PathExpanderBuilder.allTypesAndDirections;
import static org.neo4j.graphdb.traversal.Evaluation.EXCLUDE_AND_CONTINUE;
import static org.neo4j.graphdb.traversal.Evaluation.INCLUDE_AND_PRUNE;
import static org.neo4j.graphdb.traversal.Evaluators.includingDepths;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.PathExpanderBuilder;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.traversal.Evaluation;
import org.neo4j.graphdb.traversal.Evaluator;
import org.springframework.util.Assert;

import de.airsupply.commons.core.neo4j.annotation.Unique.UniquenessTraverser;
import de.airsupply.commons.core.neo4j.annotation.Unique.UniquenessTraverserFactory;
import de.airsupply.commons.core.util.CollectionUtils.Function;
import de.airsupply.commons.core.util.Pair;

public class Traversers {

	public static class ChartPositionUniquenessTraverserFactory implements UniquenessTraverserFactory {

		@Override
		public UniquenessTraverser create(Map<String, Object> parameters) {
			Node start = (Node) parameters.get("chartState");
			Node end = (Node) parameters.get("song");
			List<Pair<String, Direction>> relationships = new ArrayList<>();
			relationships.add(Pair.of("CHART_POSITIONS", BOTH));
			relationships.add(Pair.of("CHART_POSITION", OUTGOING));

			return new StartToEndUniquessTraverser(start, end, relationships, "position");
		}

	}

	public static class ShowBroadcastUniquenessTraverserFactory implements UniquenessTraverserFactory {

		@Override
		public UniquenessTraverser create(Map<String, Object> parameters) {
			Node start = (Node) parameters.get("broadcastedShow");
			Node end = (Node) parameters.get("station");
			List<Pair<String, Direction>> relationships = new ArrayList<>();
			relationships.add(Pair.of("SHOW_BROADCAST_OF", INCOMING));
			relationships.add(Pair.of("BROADCAST_ON", OUTGOING));

			return new StartToEndUniquessTraverser(start, end, relationships, "from", "to");
		}

	}

	public static class SongBroadcastUniquenessTraverserFactory implements UniquenessTraverserFactory {

		@Override
		public UniquenessTraverser create(Map<String, Object> parameters) {
			Node start = (Node) parameters.get("broadcastedSong");
			Node end = (Node) parameters.get("station");
			List<Pair<String, Direction>> relationships = new ArrayList<>();
			relationships.add(Pair.of("SONG_BROADCAST_OF", INCOMING));
			relationships.add(Pair.of("BROADCAST_ON", OUTGOING));

			return new StartToEndUniquessTraverser(start, end, relationships, "from", "to");
		}

	}

	private static class StartToEndUniquessTraverser implements UniquenessTraverser {

		private Node end;

		private List<Pair<String, Direction>> relationships;

		private Node start;

		private String[] where;

		public StartToEndUniquessTraverser(Node start, Node end, List<Pair<String, Direction>> relationships,
				String... where) {
			Assert.notNull(start);
			Assert.notNull(end);
			this.start = start;
			this.end = end;
			this.relationships = relationships;
			this.where = where;
		}

		@Override
		public Iterable<Node> traverse(GraphDatabaseService databaseService, final Map<String, Object> parameters) {
			final int count = relationships.size();
			PathExpanderBuilder expander = allTypesAndDirections();
			for (Pair<String, Direction> pair : relationships) {
				expander = expander.add(withName(pair.first()), pair.second());
			}
			Evaluator evaluator = new Evaluator() {

				@Override
				public Evaluation evaluate(Path path) {
					Relationship lastRelationship = path.lastRelationship();
					Pair<String, Direction> lastRelationshipDefinition = relationships.get(count - 1);
					if (lastRelationship == null
							|| !lastRelationshipDefinition.first().equals(lastRelationship.getType().name())
							|| !lastRelationship.getEndNode().equals(end)) {
						return EXCLUDE_AND_CONTINUE;
					}
					Node node = lastRelationship.getStartNode();
					for (String propertyName : where) {
						if (!parameters.get(propertyName).equals(node.getProperty(propertyName))) {
							return EXCLUDE_AND_CONTINUE;
						}
					}
					return INCLUDE_AND_PRUNE;
				}

			};

			// @formatter:off
			Iterable<Path> paths = databaseService
					.traversalDescription()
					.expand(expander.build())
					.evaluator(includingDepths(count, count))
					.evaluator(evaluator)
					.traverse(start);
			// @formatter:on

			return transform(paths, new Function<Path, Node>() {

				@Override
				public Node apply(Path path) {
					return path.lastRelationship().getStartNode();
				}

			}, false);
		}
	}

}