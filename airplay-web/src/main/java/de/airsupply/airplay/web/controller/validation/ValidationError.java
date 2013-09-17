package de.airsupply.airplay.web.controller.validation;

import java.util.ArrayList;
import java.util.List;

public class ValidationError {

	private List<ObjectError> errors = new ArrayList<>();

	public void addError(String code, String message) {
		errors.add(new ObjectError(code, message));
	}

	public void addError(String code, String field, String message) {
		errors.add(new FieldError(code, field, message));
	}

	public Iterable<ObjectError> getErrors() {
		return errors;
	}

}