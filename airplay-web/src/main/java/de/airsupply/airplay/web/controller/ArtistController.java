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

@Controller
@RequestMapping("/contents/artists")
public class ArtistController extends AbstractController<Artist, ContentService> {

	@Autowired
	public ArtistController(ContentService contentService) {
		super(Artist.class, contentService);
	}

	@RequestMapping(method = RequestMethod.GET, params = { "name" })
	@ResponseBody
	public Collection<Artist> searchByName(@RequestParam("name") String name) {
		return getService().findArtists(name, false);
	}

}