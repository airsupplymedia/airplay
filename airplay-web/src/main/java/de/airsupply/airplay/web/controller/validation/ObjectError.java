package de.airsupply.airplay.web.controller.validation;

public class ObjectError {

	private String code;

	private String message;

	public ObjectError(String code, String message) {
		this.code = code;
		this.message = message;
	}

	public String getCode() {
		return code;
	}

	public String getMessage() {
		return message;
	}

}
