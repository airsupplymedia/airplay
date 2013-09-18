package de.airsupply.airplay.web.controller.validation;

import java.util.ArrayList;
import java.util.List;

public class ValidationError {

	private List<ObjectError> errors = new ArrayList<>();

	public void addError(String objectName, String code, String message, Object[] arguments) {
		errors.add(new ObjectError(objectName, code, message, arguments));
	}

	public void addError(String objectName, String code, String message, Object[] arguments, String field) {
		errors.add(new FieldError(objectName, code, message, arguments, field));
	}

	public Iterable<ObjectError> getErrors() {
		return errors;
	}

}