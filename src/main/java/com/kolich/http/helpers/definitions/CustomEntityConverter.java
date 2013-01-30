package com.kolich.http.helpers.definitions;

public interface CustomEntityConverter<F,S> extends
	CustomSuccessEntityConverter<S>, CustomFailureEntityConverter<F> {
	
}
