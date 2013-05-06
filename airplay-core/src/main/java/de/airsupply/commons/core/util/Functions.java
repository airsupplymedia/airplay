package de.airsupply.commons.core.util;

import org.neo4j.graphdb.Node;

import de.airsupply.airplay.core.model.PersistentNode;
import de.airsupply.commons.core.util.CollectionUtils.Function;

public class Functions {

	public static <S extends Node> Function<S, Long> toId() {
		return new Function<S, Long>() {

			@Override
			public Long apply(Node source) {
				return Long.valueOf(source.getId());
			}

		};
	}

	public static <S extends PersistentNode> Function<S, Long> toIdentifier() {
		return new Function<S, Long>() {

			@Override
			public Long apply(PersistentNode source) {
				return source.getIdentifier();
			}

		};
	}

}
