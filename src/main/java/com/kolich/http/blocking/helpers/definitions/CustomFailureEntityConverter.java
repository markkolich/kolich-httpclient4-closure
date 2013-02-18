package com.kolich.http.blocking.helpers.definitions;

import com.kolich.http.common.response.HttpFailure;

public interface CustomFailureEntityConverter<F> {
	
	public F failure(final HttpFailure failure);
	
}