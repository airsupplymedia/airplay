package de.airsupply.airplay.core.model;

import java.util.Map;

import javax.validation.constraints.NotNull;

import org.neo4j.graphalgo.GraphAlgoFactory;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.Expander;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.kernel.Traversal;
import org.springframework.data.neo4j.annotation.Fetch;
import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.data.neo4j.annotation.RelatedTo;

import com.fasterxml.jackson.annotation.JsonIgnore;

import de.airsupply.airplay.core.model.ChartPosition.ChartPositionUniquenessTraverser;
import de.airsupply.commons.core.neo4j.annotation.Unique;
import de.airsupply.commons.core.neo4j.annotation.Unique.UniquenessTraverser;
import de.airsupply.commons.core.util.CollectionUtils;
import de.airsupply.commons.core.util.CollectionUtils.Function;

@Unique(traverser = ChartPositionUniquenessTraverser.class, parameters = { "chartState", "position", "song" })
@NodeEntity
@SuppressWarnings("serial")
public class ChartPosition extends PersistentNode {

	public static class ChartPositionUniquenessTraverser implements UniquenessTraverser {

		@Override
		public Iterable<Node> traverse(final Map<String, Object> parameters) {
			final DynamicRelationshipType chartPositionsType = DynamicRelationshipType.withName("CHART_POSITIONS");
			final DynamicRelationshipType chartPositionType = DynamicRelationshipType.withName("CHART_POSITION");

			final Integer position = Integer.valueOf(parameters.get("position").toString());

			final Node chartState = (Node) parameters.get("chartState");
			final Node song = (Node) parameters.get("song");

			Expander expander = Traversal.expanderForTypes(chartPositionsType, Direction.BOTH, chartPositionType,
					Direction.OUTGOING);
			Iterable<Path> paths = GraphAlgoFactory.pathsWithLength(expander, 2).findAllPaths(chartState, song);
			return CollectionUtils.transform(paths, new ChartPositionWithPositionFunction(position), false);
		}

	}

	private static class ChartPositionWithPositionFunction implements Function<Path, Node> {

		private final Integer position;

		private ChartPositionWithPositionFunction(Integer position) {
			this.position = position;
		}

		@Override
		public Node apply(Path path) {
			if (position.equals(path.lastRelationship().getStartNode().getProperty("position"))) {
				return path.lastRelationship().getStartNode();
			}
			return null;
		}
	}

	@NotNull
	@RelatedTo(direction = Direction.INCOMING, type = "CHART_POSITIONS")
	@JsonIgnore
	private ChartState chartState;

	private int position;

	@Fetch
	@NotNull
	@RelatedTo(direction = Direction.OUTGOING, type = "CHART_POSITION")
	private Song song;

	ChartPosition() {
		super();
	}

	public ChartPosition(ChartState chartState, Song song, int position) {
		super();
		this.chartState = chartState;
		this.song = song;
		this.position = position;
	}

	@JsonIgnore
	public ChartState getChartState() {
		return chartState;
	}

	public int getPosition() {
		return position;
	}

	public Song getSong() {
		return song;
	}

	@Override
	public String toString() {
		return "ChartPosition [chartState=" + chartState + ", position=" + position + ", song=" + song + "]";
	}

}
