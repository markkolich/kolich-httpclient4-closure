package com.kolich.http.helpers.definitions;

import com.kolich.http.common.response.HttpSuccess;

public interface CustomSuccessEntityConverter<S> {

	public S success(final HttpSuccess success) throws Exception;
	
}
