package de.airsupply.airplay.core.model;

import java.util.Date;
import java.util.Map;

import javax.validation.constraints.NotNull;

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
import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.data.neo4j.annotation.RelatedTo;

import de.airsupply.airplay.core.model.SongBroadcast.SongBroadcastUniquenessTraverser;
import de.airsupply.commons.core.neo4j.annotation.Persistent;
import de.airsupply.commons.core.neo4j.annotation.Unique;
import de.airsupply.commons.core.neo4j.annotation.Unique.UniquenessTraverser;
import de.airsupply.commons.core.util.CollectionUtils;
import de.airsupply.commons.core.util.CollectionUtils.Function;

@Unique(traverser = SongBroadcastUniquenessTraverser.class, parameters = { "broadcastedSong", "station", "from", "to" })
@NodeEntity
@SuppressWarnings("serial")
public class SongBroadcast extends Broadcast {

	public static class SongBroadcastUniquenessTraverser implements UniquenessTraverser {

		private static class SongBroadcastUniquenessEvaluator implements Evaluator {
			private final DynamicRelationshipType broadcastOnType;
			private final Long from;
			private final Node station;
			private final Long to;

			private SongBroadcastUniquenessEvaluator(Node station, Long to, DynamicRelationshipType broadcastOnType,
					Long from) {
				this.station = station;
				this.to = to;
				this.broadcastOnType = broadcastOnType;
				this.from = from;
			}

			@Override
			public Evaluation evaluate(Path path) {
				Relationship lastRelationship = path.lastRelationship();
				if (lastRelationship == null || !lastRelationship.getEndNode().equals(station)) {
					return Evaluation.EXCLUDE_AND_CONTINUE;
				}
				Node startNode = lastRelationship.getStartNode();
				if (broadcastOnType.name().equals(lastRelationship.getType().name())
						&& from.equals(startNode.getProperty("from")) && to.equals(startNode.getProperty("to"))) {
					return Evaluation.INCLUDE_AND_PRUNE;
				}
				return Evaluation.EXCLUDE_AND_CONTINUE;
			}
		}

		@Override
		public Iterable<Node> traverse(final Map<String, Object> parameters) {
			final DynamicRelationshipType broadcastOfType = DynamicRelationshipType.withName("SONG_BROADCAST_OF");
			final DynamicRelationshipType broadcastOnType = DynamicRelationshipType.withName("BROADCAST_ON");

			final Long from = Long.valueOf(parameters.get("from").toString());
			final Long to = Long.valueOf(parameters.get("to").toString());

			final Node broadcastedSong = (Node) parameters.get("broadcastedSong");
			final Node station = (Node) parameters.get("station");

			Expander expander = Traversal.expanderForTypes(broadcastOfType, Direction.INCOMING, broadcastOnType,
					Direction.OUTGOING);
			Iterable<Path> paths = Traversal.description().expand(expander).evaluator(Evaluators.includingDepths(2, 2))
					.evaluator(new SongBroadcastUniquenessEvaluator(station, to, broadcastOnType, from))
					.traverse(broadcastedSong);

			return CollectionUtils.transform(paths, new Function<Path, Node>() {

				@Override
				public Node apply(Path path) {
					return path.lastRelationship().getStartNode();
				}
			}, false);
		}

	}

	@NotNull
	@Persistent
	@RelatedTo(direction = Direction.OUTGOING, type = "SONG_BROADCAST_OF")
	private Song broadcastedSong;

	SongBroadcast() {
		super();
	}

	public SongBroadcast(Station station, Song broadcastedSong, Date time) {
		super(station, BroadcastType.EXACT_BEGIN, time, 1);
		this.broadcastedSong = broadcastedSong;
	}

	public SongBroadcast(Station station, Song broadcastedSong, Date week, int count) {
		super(station, BroadcastType.WEEK, week, count);
		this.broadcastedSong = broadcastedSong;
	}

	public Song getBroadcastedSong() {
		return broadcastedSong;
	}

	@Override
	public String toString() {
		return "SongBroadcast [broadcastedSong=" + broadcastedSong + ", getBroadcastType()=" + getBroadcastType()
				+ ", getCount()=" + getCount() + ", getFromDate()=" + getFromDate() + ", getStation()=" + getStation()
				+ ", getToDate()=" + getToDate() + ", getIdentifier()=" + getIdentifier() + "]";
	}

}
