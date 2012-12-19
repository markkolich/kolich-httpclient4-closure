package com.kolich.http.helpers.definitions;

import com.kolich.http.HttpClient4Closure.HttpSuccess;

public interface CustomEntityConverter<T> {

	public T convert(final HttpSuccess success) throws Exception;
	
}
