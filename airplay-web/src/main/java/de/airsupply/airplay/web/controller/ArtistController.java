package de.airsupply.airplay.web.controller;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import de.airsupply.airplay.core.model.Artist;
import de.airsupply.airplay.core.services.ContentService;
import de.airsupply.commons.core.neo4j.Neo4jServiceSupport;

@Controller
@RequestMapping("/artists")
public class ArtistController extends AbstractController<Artist> {

	private ContentService contentService;

	@Autowired
	public ArtistController(ContentService contentService) {
		super(Artist.class);
		this.contentService = contentService;
	}

	@Override
	protected Neo4jServiceSupport getService() {
		return contentService;
	}

	@RequestMapping(method = RequestMethod.GET, params = { "name" })
	@ResponseBody
	public Collection<Artist> searchByName(@RequestParam("name") String name) {
		return contentService.findArtists(name, false);
	}

}