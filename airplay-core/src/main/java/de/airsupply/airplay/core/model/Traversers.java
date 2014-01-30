package de.airsupply.airplay.core.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.Expander;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.traversal.Evaluation;
import org.neo4j.graphdb.traversal.Evaluator;
import org.neo4j.graphdb.traversal.Evaluators;
import org.neo4j.kernel.Traversal;
import org.springframework.util.Assert;

import de.airsupply.commons.core.neo4j.annotation.Unique.UniquenessTraverser;
import de.airsupply.commons.core.neo4j.annotation.Unique.UniquenessTraverserFactory;
import de.airsupply.commons.core.util.CollectionUtils;
import de.airsupply.commons.core.util.CollectionUtils.Function;
import de.airsupply.commons.core.util.Pair;

public class Traversers {

	public static class ChartPositionUniquenessTraverserFactory implements UniquenessTraverserFactory {

		@Override
		public UniquenessTraverser create(Map<String, Object> parameters) {
			Node start = (Node) parameters.get("chartState");
			Node end = (Node) parameters.get("song");

			List<Pair<String, Direction>> relationships = new ArrayList<>();
			relationships.add(Pair.of("CHART_POSITIONS", Direction.BOTH));
			relationships.add(Pair.of("CHART_POSITION", Direction.OUTGOING));

			return new StartToEndUniquessTraverser(start, end, relationships, "position");
		}

	}

	public static class ShowBroadcastUniquenessTraverserFactory implements UniquenessTraverserFactory {

		@Override
		public UniquenessTraverser create(Map<String, Object> parameters) {
			Node start = (Node) parameters.get("broadcastedShow");
			Node end = (Node) parameters.get("station");

			List<Pair<String, Direction>> relationships = new ArrayList<>();
			relationships.add(Pair.of("SHOW_BROADCAST_OF", Direction.INCOMING));
			relationships.add(Pair.of("BROADCAST_ON", Direction.OUTGOING));

			return new StartToEndUniquessTraverser(start, end, relationships, "from", "to");
		}

	}

	public static class SongBroadcastUniquenessTraverserFactory implements UniquenessTraverserFactory {

		@Override
		public UniquenessTraverser create(Map<String, Object> parameters) {
			Node start = (Node) parameters.get("broadcastedSong");
			Node end = (Node) parameters.get("station");

			List<Pair<String, Direction>> relationships = new ArrayList<>();
			relationships.add(Pair.of("SONG_BROADCAST_OF", Direction.INCOMING));
			relationships.add(Pair.of("BROADCAST_ON", Direction.OUTGOING));

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
		public Iterable<Node> traverse(final Map<String, Object> parameters) {
			final int count = relationships.size();
			Expander expander = Traversal.emptyExpander();
			for (Pair<String, Direction> pair : relationships) {
				expander = expander.add(DynamicRelationshipType.withName(pair.first()), pair.second());
			}
			Evaluator evaluator = new Evaluator() {

				@Override
				public Evaluation evaluate(Path path) {
					Relationship lastRelationship = path.lastRelationship();
					Pair<String, Direction> lastRelationshipDefinition = relationships.get(count - 1);
					if (lastRelationship == null
							|| !lastRelationshipDefinition.first().equals(lastRelationship.getType().name())
							|| !lastRelationship.getEndNode().equals(end)) {
						return Evaluation.EXCLUDE_AND_CONTINUE;
					}
					Node node = lastRelationship.getStartNode();
					for (String propertyName : where) {
						if (!parameters.get(propertyName).equals(node.getProperty(propertyName))) {
							return Evaluation.EXCLUDE_AND_CONTINUE;
						}
					}
					return Evaluation.INCLUDE_AND_PRUNE;
				}

			};

			// @formatter:off
			Iterable<Path> paths = Traversal
					.description()
					.expand(expander)
					.evaluator(Evaluators.includingDepths(count, count))
					.evaluator(evaluator)
					.traverse(start);
			// @formatter:on

			return CollectionUtils.transform(paths, new Function<Path, Node>() {

				@Override
				public Node apply(Path path) {
					return path.lastRelationship().getStartNode();
				}

			}, false);
		}
	}

}