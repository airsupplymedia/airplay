package de.airsupply.airplay.web.controller;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import de.airsupply.airplay.core.model.Song;
import de.airsupply.airplay.core.services.ContentService;

@Controller
@RequestMapping("/contents/songs")
public class SongController extends AbstractController<Song, ContentService> {

	@Autowired
	public SongController(ContentService contentService) {
		super(Song.class, contentService);
	}

	@RequestMapping(method = RequestMethod.GET, params = { "name" })
	@ResponseBody
	public Collection<Song> searchByName(@RequestParam("name") String name) {
		return getService().findSongs(name, false);
	}

}