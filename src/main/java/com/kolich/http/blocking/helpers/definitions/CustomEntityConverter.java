package com.kolich.http.blocking.helpers.definitions;

public interface CustomEntityConverter<F,S> extends
	CustomSuccessEntityConverter<S>, CustomFailureEntityConverter<F> {
	
}
