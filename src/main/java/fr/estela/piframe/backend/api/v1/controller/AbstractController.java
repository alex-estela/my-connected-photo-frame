package fr.estela.piframe.backend.api.v1.controller;

import io.swagger.inflector.models.ApiError;
import io.swagger.inflector.utils.ApiException;

public abstract class AbstractController {

	protected ApiException newApiException(String message) {
		ApiError error = new ApiError();
		error.setCode(500);
		error.setMessage(message);
		return new ApiException(error);
	}
}