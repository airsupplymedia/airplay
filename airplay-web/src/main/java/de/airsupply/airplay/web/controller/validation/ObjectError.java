package de.airsupply.airplay.web.controller.validation;

public class ObjectError {

	private Object[] arguments;

	private String code;

	private String message;

	private String objectName;

	public ObjectError(String objectName, String code, String message, Object[] arguments) {
		this.objectName = objectName;
		this.code = code;
		this.message = message;
		this.arguments = arguments;
	}

	public String getCode() {
		return code;
	}

	public Object[] getArguments() {
		return arguments;
	}

	public String getObjectName() {
		return objectName;
	}

	public String getMessage() {
		return message;
	}

}
