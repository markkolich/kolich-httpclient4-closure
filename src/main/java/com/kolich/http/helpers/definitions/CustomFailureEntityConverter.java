package com.kolich.http.helpers.definitions;

import com.kolich.http.HttpClient4Closure.HttpFailure;

public interface CustomFailureEntityConverter<F> {
	
	public F failure(final HttpFailure failure);
	
}
