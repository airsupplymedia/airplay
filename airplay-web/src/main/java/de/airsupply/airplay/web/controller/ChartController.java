package de.airsupply.airplay.web.controller;

import java.util.Collection;
import java.util.Date;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import de.airsupply.airplay.core.model.Chart;
import de.airsupply.airplay.core.model.ChartPosition;
import de.airsupply.airplay.core.model.Song;
import de.airsupply.airplay.core.services.ChartService;
import de.airsupply.airplay.core.services.ContentService;

@Controller
@RequestMapping("/charts")
public class ChartController {

	private ChartService chartService;

	private ContentService contentService;

	@Autowired
	public ChartController(ChartService chartService, ContentService contentService) {
		super();
		this.chartService = chartService;
		this.contentService = contentService;
	}

	@RequestMapping(method = RequestMethod.POST)
	@ResponseBody
	@ResponseStatus(HttpStatus.CREATED)
	public String createChartPostion(@PathVariable Long identifier,
			@DateTimeFormat(pattern = "YYYY-'W'ww") @PathVariable Date date, @Valid @RequestBody ChartPosition object) {

		System.out.println("AbstractController.create()");
		System.out.println(object);
		return "Created: " + object.toString();
	}

	@RequestMapping("/{identifier}")
	@ResponseBody
	public Chart find(@PathVariable Long identifier) {
		return chartService.find(identifier, Chart.class);
	}

	@RequestMapping("/{identifier}/date/{date}")
	@ResponseBody
	public Collection<ChartPosition> findChartPositions(@PathVariable Long identifier,
			@DateTimeFormat(pattern = "YYYY-'W'ww") @PathVariable Date date) {
		Chart chart = chartService.find(identifier, Chart.class);
		if (chart != null) {
			return chartService.findChartPositions(chart, date);
		}
		return null;
	}

	@RequestMapping("/{chartIdentifier}/song/{songIdentifier}")
	@ResponseBody
	public Collection<ChartPosition> findChartPositions(@PathVariable Long chartIdentifier,
			@PathVariable Long songIdentifier) {
		Chart chart = chartService.find(chartIdentifier, Chart.class);
		if (chart == null) {
			return null;
		}
		Song song = contentService.find(songIdentifier, Song.class);
		if (song == null) {
			return null;
		}
		return chartService.findChartPositions(chart, song);
	}

	@RequestMapping("/{identifier}/date/latest")
	@ResponseBody
	public Collection<ChartPosition> findLatestChartPositions(@PathVariable Long identifier) {
		Chart chart = chartService.find(identifier, Chart.class);
		if (chart != null) {
			return chartService.findLatestChartPositions(chart);
		}
		return null;
	}

	@RequestMapping(method = RequestMethod.GET)
	@ResponseBody
	public Collection<Chart> getCharts() {
		return chartService.getCharts();
	}

}