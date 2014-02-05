package de.airsupply.airplay.core.test.misc;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.neo4j.conversion.Result;
import org.springframework.data.neo4j.support.Neo4jTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import de.airsupply.commons.core.context.Loggable;
import de.airsupply.commons.core.util.CollectionUtils.Procedure;

@Component
public class CypherQueryShell {

	@Loggable
	private static Logger logger;

	public static void main(String[] args) {
		BatchRunner.run(new Procedure<ApplicationContext>() {

			@Override
			public void run(ApplicationContext applicationContext) {
				try {
					applicationContext.getBean(CypherQueryShell.class).run();
				} catch (Exception exception) {
					logger.error(exception.getMessage(), exception);
				}
			}

		}, "test");
	}

	@Autowired
	private Neo4jTemplate neo4jTemplate;

	private void run() {
		StopWatch stopWatch = new StopWatch();
		stopWatch.start("Running query");
		Map<String, Object> params = new HashMap<>();
		Result<Map<String, Object>> result = neo4jTemplate.query("START result=node(1) RETURN result", params);

		stopWatch.stop();
		stopWatch.start("Printing results");

		Iterator<Map<String, Object>> iterator = result.iterator();
		while (iterator.hasNext()) {
			logger.info(iterator.next().toString());
		}
		stopWatch.stop();
		logger.info(stopWatch.prettyPrint());
	}
}
