package com.utils;

import java.util.ArrayList;
import java.util.List;

import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

import com.errors.FieldErrorDTO;

public class ValidationUtil {
	public static List<FieldErrorDTO> validateErrors(BindingResult errors) {
		List<FieldErrorDTO> fieldErrors = new ArrayList<>();

		if (errors.hasErrors()) {
			for (ObjectError error : errors.getAllErrors()) {
				if (error instanceof FieldError) {
					FieldError fieldError = (FieldError) error;
					String field = fieldError.getField();
					String errorMessage = fieldError.getDefaultMessage();
					fieldErrors.add(new FieldErrorDTO(field, errorMessage));
				}
			}
		}

		return fieldErrors;
	}
}
