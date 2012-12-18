package com.kolich.http.helpers.definitions;

import static com.kolich.http.KolichDefaultHttpClient.KolichHttpClientFactory.getNewInstanceNoProxySelector;

import org.apache.http.client.HttpClient;

import com.kolich.http.HttpClient4Closure;

public abstract class OrExceptionClosure<S> extends HttpClient4Closure<Exception,S> {

	public OrExceptionClosure(final HttpClient client) {
		super(client);
	}
	
	public OrExceptionClosure() {
		this(getNewInstanceNoProxySelector());
	}
	
	@Override
	public final Exception failure(final HttpFailure failure) {
		return failure.getCause();
	}

}
