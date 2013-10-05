package de.airsupply.airplay.core.model;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Collections;
import java.util.List;

import org.hibernate.validator.constraints.NotEmpty;
import org.neo4j.graphdb.Direction;
import org.springframework.data.neo4j.annotation.Indexed;
import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.data.neo4j.annotation.RelatedTo;

import com.fasterxml.jackson.annotation.JsonIgnore;

import de.airsupply.commons.core.neo4j.annotation.Unique;
import de.airsupply.commons.core.util.CollectionUtils;

@Unique(arguments = { "name" })
@NodeEntity
@SuppressWarnings("serial")
public class Chart extends PersistentNode {

	@RelatedTo(direction = Direction.OUTGOING, type = "CHART_STATES")
	@JsonIgnore
	private Iterable<ChartState> chartStates = null;

	@NotEmpty
	@Indexed
	private String name;

	Chart() {
		super();
	}

	public Chart(String name) {
		super();
		this.name = name;
	}

	public List<ChartState> getChartStateList() {
		if (chartStates != null) {
			return CollectionUtils.asList(chartStates);
		} else {
			return Collections.emptyList();
		}
	}

	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return "Chart [name=" + name + ", getIdentifier()=" + getIdentifier() + "]";
	}

	private void writeObject(ObjectOutputStream outputStream) throws IOException {
		chartStates = null;
		outputStream.defaultWriteObject();
	}

}
