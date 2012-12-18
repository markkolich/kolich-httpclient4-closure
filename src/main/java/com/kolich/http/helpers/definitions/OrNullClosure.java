package com.kolich.http.helpers.definitions;

import static com.kolich.http.KolichDefaultHttpClient.KolichHttpClientFactory.getNewInstanceNoProxySelector;

import org.apache.http.client.HttpClient;

import com.kolich.http.HttpClient4Closure;

public abstract class OrNullClosure<S> extends HttpClient4Closure<Void,S> {

	public OrNullClosure(final HttpClient client) {
		super(client);
	}
	
	public OrNullClosure() {
		this(getNewInstanceNoProxySelector());
	}
	
	@Override
	public final Void failure(final HttpFailure failure) {
		return null;
	}

}
