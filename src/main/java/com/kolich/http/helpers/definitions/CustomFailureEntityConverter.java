package com.kolich.http.helpers.definitions;

import com.kolich.http.common.response.HttpFailure;

public interface CustomFailureEntityConverter<F> {
	
	public F failure(final HttpFailure failure);
	
}
