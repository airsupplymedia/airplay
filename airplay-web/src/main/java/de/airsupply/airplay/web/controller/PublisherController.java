package de.airsupply.airplay.web.controller;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import de.airsupply.airplay.core.model.Publisher;
import de.airsupply.airplay.core.services.ContentService;

@Controller
@RequestMapping("/contents/publishers")
public class PublisherController extends AbstractController<Publisher, ContentService> {

	@Autowired
	public PublisherController(ContentService contentService) {
		super(Publisher.class, contentService);
	}

	@RequestMapping(method = RequestMethod.GET, params = { "name" })
	@ResponseBody
	public Collection<Publisher> searchByName(@RequestParam("name") String name) {
		return getService().findPublishers(name);
	}

}