package de.airsupply.airplay.core.model;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Expander;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.PathExpander;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.traversal.BranchState;
import org.neo4j.graphdb.traversal.Evaluation;
import org.neo4j.graphdb.traversal.Evaluator;
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

import de.airsupply.commons.core.neo4j.annotation.Persistent;
import de.airsupply.commons.core.neo4j.annotation.Unique;
import de.airsupply.commons.core.util.CollectionUtils;
import de.airsupply.commons.core.util.Functions;

@Unique(arguments = { "week" })
@NodeEntity
@SuppressWarnings("serial")
public class RecordImport extends PersistentNode {

	private static class DependeesFieldTraversalBuilder implements FieldTraversalDescriptionBuilder {

		@Override
		public TraversalDescription build(Object start, Neo4jPersistentProperty property, String... params) {
			PathExpander<?> expander = new ImportedRecordPathExpander();
			Evaluator evaluator = new ImportedRecordEvaluator();
			return Traversal.description().expand(expander).evaluator(evaluator);
		}
	}

	private static class ImportedNodesFieldTraversalBuilder implements FieldTraversalDescriptionBuilder {

		@Override
		public TraversalDescription build(Object start, Neo4jPersistentProperty property, String... params) {
			Expander expander = Traversal.expanderForAllTypes(Direction.OUTGOING);
			return Traversal.description().expand(expander).evaluator(Evaluators.toDepth(1));
		}
	}

	private static class ImportedRecordEvaluator implements Evaluator {

		@Override
		public Evaluation evaluate(Path path) {
			Object systemValue = path.endNode().getProperty("system");
			if (Boolean.valueOf(Objects.toString(systemValue)).booleanValue()) {
				return Evaluation.EXCLUDE_AND_CONTINUE;
			}
			return Evaluation.INCLUDE_AND_CONTINUE;
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

	@GraphTraversal(traversal = DependeesFieldTraversalBuilder.class)
	private Iterable<Node> dependees;

	@Fetch
	@Persistent
	@RelatedTo(direction = Direction.OUTGOING, type = "IMPORTED_ARTISTS")
	private Set<Artist> importedArtists = new HashSet<>();

	@Fetch
	@Persistent
	@RelatedTo(direction = Direction.OUTGOING, type = "IMPORTED_CHART_POSITIONS")
	private Set<ChartPosition> importedChartPositions = new HashSet<>();

	@Fetch
	@Persistent
	@RelatedTo(direction = Direction.OUTGOING, type = "IMPORTED_CHART_STATES")
	private Set<ChartState> importedChartStates = new HashSet<>();

	@JsonIgnore
	@GraphTraversal(traversal = ImportedNodesFieldTraversalBuilder.class)
	private Iterable<Node> importedNodes;

	@Fetch
	@Persistent
	@RelatedTo(direction = Direction.OUTGOING, type = "IMPORTED_PUBLISHERS")
	private Set<Publisher> importedPublishers = new HashSet<>();

	@Fetch
	@Persistent
	@RelatedTo(direction = Direction.OUTGOING, type = "IMPORTED_RECORD_COMPANIES")
	private Set<RecordCompany> importedRecordCompanies = new HashSet<>();

	@Fetch
	@Persistent
	@RelatedTo(direction = Direction.OUTGOING, type = "IMPORTED_SHOW_BROADCASTS")
	private Set<ShowBroadcast> importedShowBroadcasts = new HashSet<>();

	@Fetch
	@Persistent
	@RelatedTo(direction = Direction.OUTGOING, type = "IMPORTED_SONG_BROADCASTS")
	private Set<SongBroadcast> importedSongBroadcasts = new HashSet<>();

	@Fetch
	@Persistent
	@RelatedTo(direction = Direction.OUTGOING, type = "IMPORTED_SONGS")
	private Set<Song> importedSongs = new HashSet<>();

	@Fetch
	@Persistent
	@RelatedTo(direction = Direction.OUTGOING, type = "IMPORTED_STATIONS")
	private Set<Station> importedStations = new HashSet<>();

	@Indexed
	private long week;

	RecordImport() {
		super();
	}

	public RecordImport(Date week) {
		super();
		Assert.notNull(week);
		this.week = week.getTime();
	}

	private Collection<Node> getDependees() {
		Set<Node> result = new HashSet<>(CollectionUtils.asSet(dependees));
		result.removeAll(CollectionUtils.asSet(importedNodes));
		return result;
	}

	public Collection<PersistentNode> getDependees(Neo4jTemplate neo4jTemplate) {
		Assert.notNull(neo4jTemplate);
		return CollectionUtils.transform(getDependees(), Functions.toPersistentState(neo4jTemplate));
	}

	private Node[] getDependeesAsArray() {
		Collection<Node> collection = getDependees();
		return collection.toArray(new Node[collection.size()]);
	}

	public Set<Artist> getImportedArtistList() {
		return importedArtists;
	}

	public List<ChartPosition> getImportedChartPositionList() {
		if (importedChartPositions != null) {
			return CollectionUtils.asList(importedChartPositions);
		} else {
			return Collections.emptyList();
		}
	}

	public Set<ChartState> getImportedChartStateList() {
		return importedChartStates;
	}

	private Node[] getImportedNodesAsArray() {
		Collection<Node> collection = CollectionUtils.asList(importedNodes);
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
		importedRecords.addAll(getImportedArtistList());
		importedRecords.addAll(getImportedChartPositionList());
		importedRecords.addAll(getImportedChartStateList());
		importedRecords.addAll(getImportedPublisherList());
		importedRecords.addAll(getImportedRecordCompanyList());
		importedRecords.addAll(getImportedShowBroadcastList());
		importedRecords.addAll(getImportedSongBroadcastList());
		importedRecords.addAll(getImportedSongList());
		importedRecords.addAll(getImportedStationList());
		return Collections.unmodifiableCollection(importedRecords);
	}

	@JsonIgnore
	public List<PersistentNode> getImportedRecordsWithDependees(Neo4jTemplate neo4jTemplate) {
		Collection<Node> importedRecordsWithDependencies = new HashSet<>();
		TraversalDescription traversalDescription = Traversal.description().breadthFirst()
				.expand(Traversal.pathExpanderForAllTypes(Direction.OUTGOING))
				.evaluator(Evaluators.includeWhereEndNodeIs(getImportedNodesAsArray()));
		for (Path path : traversalDescription.traverse(getDependeesAsArray())) {
			importedRecordsWithDependencies.add(path.endNode());
		}
		return CollectionUtils.transform(importedRecordsWithDependencies, Functions.toPersistentState(neo4jTemplate));
	}

	public List<PersistentNode> getImportedRecordsWithoutDependees(Neo4jTemplate neo4jTemplate) {
		List<PersistentNode> importedRecordsWithoutDependees = new ArrayList<>(getImportedRecords());
		importedRecordsWithoutDependees.removeAll(getImportedRecordsWithDependees(neo4jTemplate));
		return importedRecordsWithoutDependees;
	}

	public List<ShowBroadcast> getImportedShowBroadcastList() {
		if (importedShowBroadcasts != null) {
			return CollectionUtils.asList(importedShowBroadcasts);
		} else {
			return Collections.emptyList();
		}
	}

	public List<SongBroadcast> getImportedSongBroadcastList() {
		if (importedSongBroadcasts != null) {
			return CollectionUtils.asList(importedSongBroadcasts);
		} else {
			return Collections.emptyList();
		}
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

	public void importArtist(Artist artist) {
		importedArtists.add(artist);
	}

	public void importBroadcast(ShowBroadcast broadcast) {
		importedShowBroadcasts.add(broadcast);
	}

	public void importBroadcast(SongBroadcast broadcast) {
		importedSongBroadcasts.add(broadcast);
	}

	public void importChartPosition(ChartPosition chartPosition) {
		importedChartPositions.add(chartPosition);
	}

	public void importChartState(ChartState chartState) {
		importedChartStates.add(chartState);
	}

	public void importPublisher(Publisher publisher) {
		importedPublishers.add(publisher);
	}

	public void importRecordCompany(RecordCompany recordCompany) {
		importedRecordCompanies.add(recordCompany);
	}

	public void importSong(Song song) {
		importedSongs.add(song);
	}

	public void importStation(Station station) {
		importedStations.add(station);
	}

	private void writeObject(ObjectOutputStream outputStream) throws IOException {
		importedArtists = null;
		importedChartPositions = null;
		importedChartStates = null;
		importedPublishers = null;
		importedRecordCompanies = null;
		importedShowBroadcasts = null;
		importedSongBroadcasts = null;
		importedSongs = null;
		importedStations = null;
		outputStream.defaultWriteObject();
	}

}
