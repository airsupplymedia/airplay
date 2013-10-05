package de.airsupply.airplay.web.controller;

import java.util.Collection;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import de.airsupply.airplay.core.model.Chart;
import de.airsupply.airplay.core.model.ChartPosition;
import de.airsupply.airplay.core.services.ChartService;

@Controller
@RequestMapping("/charts")
public class ChartController {

	private ChartService chartService;

	@Autowired
	public ChartController(ChartService chartService) {
		super();
		this.chartService = chartService;
	}

	@RequestMapping("/{identifier}")
	@ResponseBody
	public Chart find(@PathVariable Long identifier) {
		return chartService.find(identifier, Chart.class);
	}

	@RequestMapping("/{identifier}/{date}")
	@ResponseBody
	public Collection<ChartPosition> findChartPositions(@PathVariable Long identifier, @PathVariable Date date) {
		Chart chart = chartService.find(identifier, Chart.class);
		if (chart != null) {
			return chartService.findChartPositions(chart, date);
		}
		return null;
	}

	@RequestMapping("/")
	@ResponseBody
	public Collection<Chart> getCharts() {
		return chartService.getCharts();
	}

}