package de.airsupply.airplay.core.model;

import static de.airsupply.commons.core.util.CollectionUtils.asList;
import static java.util.Collections.emptyList;

import java.util.List;

import org.hibernate.validator.constraints.NotEmpty;
import org.neo4j.graphdb.Direction;
import org.springframework.data.neo4j.annotation.Indexed;
import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.data.neo4j.annotation.RelatedTo;

import com.fasterxml.jackson.annotation.JsonIgnore;

import de.airsupply.commons.core.neo4j.annotation.Unique;

@Unique(parameters = { "name" })
@NodeEntity
@SuppressWarnings("serial")
public class Chart extends PersistentNode {

	@RelatedTo(direction = Direction.BOTH, type = "CHART_STATES")
	@JsonIgnore
	private Iterable<ChartState> chartStates = null;

	@NotEmpty
	@Indexed
	private String name;

	Chart() {
		super(true);
	}

	public Chart(String name) {
		super(true);
		this.name = name;
	}

	@JsonIgnore
	public List<ChartState> getChartStateList() {
		if (chartStates != null) {
			return asList(chartStates);
		} else {
			return emptyList();
		}
	}

	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return "Chart [name=" + name + ", getIdentifier()=" + getIdentifier() + "]";
	}

}
