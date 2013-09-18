package de.airsupply.airplay.web.controller.validation;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class ErrorHandler {

	private MessageSource messageSource;

	@Autowired
	public ErrorHandler(MessageSource messageSource) {
		this.messageSource = messageSource;
	}

	private String getMessage(ObjectError error) {
		return messageSource.getMessage(error, LocaleContextHolder.getLocale());
	}

	private void processFieldErrors(ValidationError validationError, List<FieldError> errors) {
		for (FieldError error : errors) {
			validationError.addError(error.getObjectName(), error.getCode(), getMessage(error), error.getArguments(),
					error.getField());
		}
	}

	private void processObjectErrors(ValidationError validationError, List<ObjectError> errors) {
		for (ObjectError error : errors) {
			validationError.addError(error.getObjectName(), error.getCode(), getMessage(error), error.getArguments());
		}
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ResponseBody
	public ValidationError processValidationError(MethodArgumentNotValidException exception) {
		BindingResult result = exception.getBindingResult();
		ValidationError validationError = new ValidationError();
		processObjectErrors(validationError, result.getGlobalErrors());
		processFieldErrors(validationError, result.getFieldErrors());
		return validationError;

	}
}