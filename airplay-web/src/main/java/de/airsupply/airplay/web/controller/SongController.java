package de.airsupply.airplay.web.controller;

import java.util.Collection;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import de.airsupply.airplay.core.model.Song;
import de.airsupply.airplay.core.services.ContentService;

@Controller
@RequestMapping("/songs")
public class SongController {

	private ContentService contentService;

	@Autowired
	public SongController(ContentService contentService) {
		this.contentService = contentService;
	}

	@RequestMapping(value = "/song/{identifier}", method = RequestMethod.DELETE)
	@ResponseBody
	public String delete(@PathVariable Long identifier) {
		System.out.println("SongController.delete()");
		System.out.println(identifier);
		return "Deleted: " + identifier;
	}

	@RequestMapping("/song/{identifier}")
	@ResponseBody
	public Song getById(@PathVariable Long identifier) {
		return contentService.getSong(identifier);
	}

	@RequestMapping("/search/{query}")
	@ResponseBody
	public Collection<Song> getBySearch(@PathVariable String query) {
		return contentService.findSongs(query, false);
	}

	@RequestMapping(value = "/song/{identifier}", method = RequestMethod.PUT)
	@ResponseBody
	public String put(@PathVariable Long identifier, @Valid @RequestBody Song song) {
		System.out.println("SongController.put()");
		System.out.println(identifier);
		System.out.println(song);
		return "Saved: " + song.toString();
	}

	@RequestMapping(value = "/song", method = RequestMethod.PUT)
	@ResponseBody
	public String put(@Valid @RequestBody Song song) {
		System.out.println("SongController.put()");
		System.out.println(song);
		return "Created: " + song.toString();
	}

}