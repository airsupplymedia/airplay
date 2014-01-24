package de.airsupply.commons.core.context;

import java.util.List;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import de.airsupply.airplay.core.model.Chart;
import de.airsupply.airplay.core.services.ChartService;

@Component
@Profile("!test")
public class BootstrapLifecyclePhase {

	@Autowired
	private ChartService chartService;

	@Loggable
	private Logger logger;

	@PostConstruct
	public void start() {
		logger.info("Initializing the database...");
		List<Chart> charts = chartService.getCharts();
		if (charts.isEmpty()) {
			logger.info("Adding the default Charts...");
			chartService.save(new Chart("Airplay Charts"));
			chartService.save(new Chart("Sales Charts"));
		} else {
			for (Chart chart : charts) {
				if (!chart.isSystem()) {
					logger.info("Setting the default Charts to 'system'...");
					chart.setSystem(true);
					chartService.save(chart);
				}
			}
		}
	}

}
