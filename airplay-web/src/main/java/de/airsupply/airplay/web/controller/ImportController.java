package de.airsupply.airplay.web.controller;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MultipartFile;

import de.airsupply.airplay.core.model.Chart;
import de.airsupply.airplay.core.model.RecordImport;
import de.airsupply.airplay.core.services.ImportService;
import de.airsupply.airplay.core.services.ImportService.ImporterType;

@Controller
@RequestMapping("/imports")
public class ImportController {

	@Autowired
	private ImportService importService;

	@RequestMapping(value = "/{identifier}", method = RequestMethod.DELETE)
	@ResponseBody
	public String delete(@PathVariable Long identifier) {
		RecordImport recordImport = getService().find(identifier, RecordImport.class);
		Assert.notNull(recordImport, "Import " + identifier + " could not be found!");
		getService().revertImport(recordImport);
		return "Reverted: " + identifier;
	}

	@RequestMapping(method = RequestMethod.GET)
	@ResponseBody
	public Collection<RecordImport> get() {
		return getService().getRecordImports();
	}

	@RequestMapping("/{identifier}")
	@ResponseBody
	public RecordImport get(@PathVariable Long identifier) {
		return getService().find(identifier, RecordImport.class);
	}

	private ImportService getService() {
		return importService;
	}

	@RequestMapping(method = RequestMethod.POST)
	@ResponseBody
	@ResponseStatus(HttpStatus.CREATED)
	public String run(@DateTimeFormat(pattern = "YYYY-'W'ww") @RequestParam Date week,
			@RequestParam Long chartIdentifier, @RequestParam("file") MultipartFile file) {
		try {
			Chart chart = getService().find(chartIdentifier, Chart.class);
			ImporterType importerType = ImporterType.getByFileName(file.getOriginalFilename());
			getService().importRecords(importerType, chart, week, file.getInputStream());
			return "success";
		} catch (IOException exception) {
			throw new RuntimeException(exception);
		}
	}

}