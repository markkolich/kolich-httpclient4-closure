package com.kolich.http.helpers.definitions;

import com.kolich.http.HttpClient4Closure.HttpFailure;
import com.kolich.http.HttpClient4Closure.HttpSuccess;

public interface CustomEntityConverter<F,S> {

	public S success(final HttpSuccess success) throws Exception;
	
	public F failure(final HttpFailure failure);
	
}
