package de.airsupply.airplay.web.controller.validation;

public class FieldError extends ObjectError {

	private String field;

	public FieldError(String code, String field, String message) {
		super(code, message);
		this.field = field;
	}

	public String getField() {
		return field;
	}

}