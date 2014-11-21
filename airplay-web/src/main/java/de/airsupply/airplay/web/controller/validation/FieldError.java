package de.airsupply.airplay.web.controller.validation;

public class FieldError extends ObjectError {

	private String field;

	public FieldError(String objectName, String code, String message, Object[] arguments, String field) {
		super(objectName, code, message, arguments);
		this.field = field;
	}

	public String getField() {
		return field;
	}

}