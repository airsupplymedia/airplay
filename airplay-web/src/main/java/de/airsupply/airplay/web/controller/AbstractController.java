package de.airsupply.airplay.web.controller;

import javax.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import de.airsupply.airplay.core.model.PersistentNode;
import de.airsupply.commons.core.neo4j.Neo4jServiceSupport;

public abstract class AbstractController<T extends PersistentNode> {

	private Class<T> type;

	public AbstractController(Class<T> type) {
		this.type = type;
	}

	@RequestMapping(method = RequestMethod.POST)
	@ResponseBody
	@ResponseStatus(HttpStatus.CREATED)
	public String create(@Valid @RequestBody T object) {
		System.out.println("AbstractController.create()");
		System.out.println(object);
		return "Created: " + object.toString();
	}

	@RequestMapping(value = "/{identifier}", method = RequestMethod.DELETE)
	@ResponseBody
	public String delete(@PathVariable Long identifier) {
		System.out.println("AbstractController.delete()");
		System.out.println(identifier);
		return "Deleted: " + identifier;
	}

	@RequestMapping("/{identifier}")
	@ResponseBody
	public T find(@PathVariable Long identifier) {
		return getService().find(identifier, type);
	}

	protected abstract Neo4jServiceSupport getService();

	@RequestMapping(value = "/{identifier}", method = RequestMethod.PUT)
	@ResponseBody
	public String put(@PathVariable Long identifier, @Valid @RequestBody T object) {
		System.out.println("AbstractController.put()");
		System.out.println(identifier);
		System.out.println(object);
		return "Saved: " + object.toString();
	}

}
