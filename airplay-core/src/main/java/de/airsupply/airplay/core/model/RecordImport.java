package de.airsupply.airplay.core.model;

import static de.airsupply.commons.core.util.CollectionUtils.asList;
import static de.airsupply.commons.core.util.CollectionUtils.asSet;
import static de.airsupply.commons.core.util.CollectionUtils.transform;
import static de.airsupply.commons.core.util.Functions.toEntity;
import static java.util.Collections.unmodifiableCollection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.validation.constraints.NotNull;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.PathExpander;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.traversal.BranchState;
import org.neo4j.graphdb.traversal.Evaluators;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.neo4j.kernel.Traversal;
import org.springframework.data.neo4j.annotation.Fetch;
import org.springframework.data.neo4j.annotation.GraphTraversal;
import org.springframework.data.neo4j.annotation.Indexed;
import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.data.neo4j.annotation.RelatedTo;
import org.springframework.data.neo4j.core.FieldTraversalDescriptionBuilder;
import org.springframework.data.neo4j.mapping.Neo4jPersistentProperty;
import org.springframework.data.neo4j.support.Neo4jTemplate;
import org.springframework.util.Assert;

import com.fasterxml.jackson.annotation.JsonIgnore;

import de.airsupply.commons.core.neo4j.QueryUtils;
import de.airsupply.commons.core.neo4j.annotation.Persistent;
import de.airsupply.commons.core.neo4j.annotation.Unique;

@Unique(query = "START chart=node({chart}) MATCH chart<-[:CHART]->recordImport WHERE recordImport.week={week} RETURN recordImport", parameters = {
		"chart", "week" })
@NodeEntity
@SuppressWarnings("serial")
public class RecordImport extends PersistentNode {

	private static class DependeesFieldTraversalBuilder implements FieldTraversalDescriptionBuilder {

		@Override
		public TraversalDescription build(Object start, Neo4jPersistentProperty property, String... params) {
			// @formatter:off
			return Traversal
					.description()
					.expand((PathExpander<?>) new ImportedRecordPathExpander())
					.evaluator(QueryUtils.getSystemNodeExcludingEvaluator());
			// @formatter:on
		}

	}

	private static class ImportedNodesFieldTraversalBuilder implements FieldTraversalDescriptionBuilder {

		@Override
		public TraversalDescription build(Object start, Neo4jPersistentProperty property, String... params) {
			// @formatter:off
			return Traversal
					.description()
					.expand(Traversal.expanderForAllTypes(Direction.OUTGOING))
					.evaluator(Evaluators.toDepth(1));
			// @formatter:on
		}

	}

	private static class ImportedRecordPathExpander implements PathExpander<Object> {

		@Override
		public Iterable<Relationship> expand(Path path, BranchState<Object> state) {
			if (path.lastRelationship() == null) {
				return path.endNode().getRelationships(Direction.OUTGOING);
			}
			return path.endNode().getRelationships(Direction.INCOMING);
		}

		@Override
		public PathExpander<Object> reverse() {
			throw new UnsupportedOperationException();
		}

	}

	@Fetch
	@NotNull
	@Persistent
	@RelatedTo(type = "CHART")
	private Chart chart;

	@GraphTraversal(traversal = DependeesFieldTraversalBuilder.class)
	private Iterable<Node> dependees;

	@Fetch
	@Persistent
	@RelatedTo(type = "IMPORTED_ARTISTS")
	private Set<Artist> importedArtists = new HashSet<>();

	@Fetch
	@Persistent
	@RelatedTo(type = "IMPORTED_CHART_POSITIONS")
	private Set<ChartPosition> importedChartPositions = new HashSet<>();

	@Fetch
	@Persistent
	@RelatedTo(type = "IMPORTED_CHART_STATES")
	private Set<ChartState> importedChartStates = new HashSet<>();

	@JsonIgnore
	@GraphTraversal(traversal = ImportedNodesFieldTraversalBuilder.class)
	private Iterable<Node> importedNodes;

	@Fetch
	@Persistent
	@RelatedTo(type = "IMPORTED_PUBLISHERS")
	private Set<Publisher> importedPublishers = new HashSet<>();

	@Fetch
	@Persistent
	@RelatedTo(type = "IMPORTED_RECORD_COMPANIES")
	private Set<RecordCompany> importedRecordCompanies = new HashSet<>();

	@Fetch
	@Persistent
	@RelatedTo(type = "IMPORTED_SHOW_BROADCASTS")
	private Set<ShowBroadcast> importedShowBroadcasts = new HashSet<>();

	@Fetch
	@Persistent
	@RelatedTo(type = "IMPORTED_SONG_BROADCASTS")
	private Set<SongBroadcast> importedSongBroadcasts = new HashSet<>();

	@Fetch
	@Persistent
	@RelatedTo(type = "IMPORTED_SONGS")
	private Set<Song> importedSongs = new HashSet<>();

	@Fetch
	@Persistent
	@RelatedTo(type = "IMPORTED_STATIONS")
	private Set<Station> importedStations = new HashSet<>();

	@Indexed
	private long week;

	RecordImport() {
		super();
	}

	public RecordImport(Chart chart, Date week) {
		super();
		Assert.notNull(chart);
		Assert.notNull(week);
		this.chart = chart;
		this.week = week.getTime();
	}

	public Chart getChart() {
		return chart;
	}

	private Collection<Node> getDependees() {
		Set<Node> result = new HashSet<>(asSet(dependees));
		result.removeAll(asSet(importedNodes));
		return result;
	}

	public Collection<PersistentNode> getDependees(Neo4jTemplate neo4jTemplate) {
		Assert.notNull(neo4jTemplate);
		return transform(getDependees(), toEntity(neo4jTemplate));
	}

	private Node[] getDependeesAsArray() {
		Collection<Node> collection = getDependees();
		return collection.toArray(new Node[collection.size()]);
	}

	public Set<Artist> getImportedArtistList() {
		return importedArtists;
	}

	public Set<ChartPosition> getImportedChartPositionList() {
		return importedChartPositions;
	}

	public Set<ChartState> getImportedChartStateList() {
		return importedChartStates;
	}

	private Node[] getImportedNodesAsArray() {
		Collection<Node> collection = asList(importedNodes);
		return collection.toArray(new Node[collection.size()]);
	}

	public Set<Publisher> getImportedPublisherList() {
		return importedPublishers;
	}

	public Set<RecordCompany> getImportedRecordCompanyList() {
		return importedRecordCompanies;
	}

	public int getImportedRecordCount() {
		return getImportedRecords().size();
	}

	@JsonIgnore
	public Collection<PersistentNode> getImportedRecords() {
		Collection<PersistentNode> importedRecords = new HashSet<>();
		importedRecords.addAll(getImportedChartStateList());
		importedRecords.addAll(getImportedChartPositionList());
		importedRecords.addAll(getImportedShowBroadcastList());
		importedRecords.addAll(getImportedSongBroadcastList());
		importedRecords.addAll(getImportedSongList());
		importedRecords.addAll(getImportedStationList());
		importedRecords.addAll(getImportedArtistList());
		importedRecords.addAll(getImportedPublisherList());
		importedRecords.addAll(getImportedRecordCompanyList());
		return unmodifiableCollection(importedRecords);
	}

	@JsonIgnore
	public List<PersistentNode> getImportedRecordsWithDependees(Neo4jTemplate neo4jTemplate) {
		Collection<Node> importedRecordsWithDependencies = new HashSet<>();
		// @formatter:off
		TraversalDescription traversalDescription = Traversal
				.description()
				.breadthFirst()
				.expand(Traversal.pathExpanderForAllTypes(Direction.OUTGOING))
				.evaluator(Evaluators.includeWhereEndNodeIs(getImportedNodesAsArray()))
				.evaluator(QueryUtils.getSystemNodeExcludingEvaluator());
		// @formatter:on
		for (Path path : traversalDescription.traverse(getDependeesAsArray())) {
			importedRecordsWithDependencies.add(path.endNode());
		}
		return transform(importedRecordsWithDependencies, toEntity(neo4jTemplate));
	}

	public List<PersistentNode> getImportedRecordsWithoutDependees(Neo4jTemplate neo4jTemplate) {
		List<PersistentNode> importedRecordsWithoutDependees = new ArrayList<>(getImportedRecords());
		importedRecordsWithoutDependees.removeAll(getImportedRecordsWithDependees(neo4jTemplate));
		return importedRecordsWithoutDependees;
	}

	public Set<ShowBroadcast> getImportedShowBroadcastList() {
		return importedShowBroadcasts;
	}

	public Set<SongBroadcast> getImportedSongBroadcastList() {
		return importedSongBroadcasts;
	}

	public Set<Song> getImportedSongList() {
		return importedSongs;
	}

	public Set<Station> getImportedStationList() {
		return importedStations;
	}

	public Date getWeekDate() {
		return new Date(week);
	}

	public <T extends PersistentNode> void importRecord(T object) {
		Assert.notNull(object);

		if (object instanceof Artist) {
			importedArtists.add((Artist) object);
		} else if (object instanceof ChartPosition) {
			importedChartPositions.add((ChartPosition) object);
		} else if (object instanceof ChartState) {
			importedChartStates.add((ChartState) object);
		} else if (object instanceof Publisher) {
			importedPublishers.add((Publisher) object);
		} else if (object instanceof RecordCompany) {
			importedRecordCompanies.add((RecordCompany) object);
		} else if (object instanceof ShowBroadcast) {
			importedShowBroadcasts.add((ShowBroadcast) object);
		} else if (object instanceof SongBroadcast) {
			importedSongBroadcasts.add((SongBroadcast) object);
		} else if (object instanceof Song) {
			importedSongs.add((Song) object);
		} else if (object instanceof ShowBroadcast) {
			importedShowBroadcasts.add((ShowBroadcast) object);
		} else if (object instanceof SongBroadcast) {
			importedSongBroadcasts.add((SongBroadcast) object);
		} else if (object instanceof Station) {
			importedStations.add((Station) object);
		} else {
			throw new IllegalArgumentException();
		}
	}

	public boolean isReversible() {
		return getDependees().isEmpty();
	}

	@Override
	public String toString() {
		return "RecordImport [chart=" + chart + ", week=" + week + "]";
	}

}
