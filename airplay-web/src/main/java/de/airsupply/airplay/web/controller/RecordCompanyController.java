package de.airsupply.airplay.web.controller;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import de.airsupply.airplay.core.model.RecordCompany;
import de.airsupply.airplay.core.services.ContentService;

@Controller
@RequestMapping("/contents/recordCompanies")
public class RecordCompanyController extends AbstractController<RecordCompany, ContentService> {

	@Autowired
	public RecordCompanyController(ContentService contentService) {
		super(RecordCompany.class, contentService);
	}

	@RequestMapping(method = RequestMethod.GET, params = { "name" })
	@ResponseBody
	public Collection<RecordCompany> searchByName(@RequestParam("name") String name) {
		return getService().findRecordCompanies(name);
	}

}