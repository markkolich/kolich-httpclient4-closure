package com.kolich.http.helpers.definitions;

import com.kolich.http.HttpClient4Closure.HttpSuccess;

public interface CustomSuccessEntityConverter<S> {

	public S success(final HttpSuccess success) throws Exception;
	
}
