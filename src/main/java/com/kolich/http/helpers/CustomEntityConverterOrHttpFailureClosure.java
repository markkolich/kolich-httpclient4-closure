package com.kolich.http.helpers;

import static com.kolich.http.KolichDefaultHttpClient.KolichHttpClientFactory.getNewInstanceWithProxySelector;

import org.apache.http.client.HttpClient;

import com.kolich.http.helpers.definitions.CustomEntityConverter;
import com.kolich.http.helpers.definitions.OrHttpFailureClosure;

public class CustomEntityConverterOrHttpFailureClosure<S> extends OrHttpFailureClosure<S> {
	
	private final CustomEntityConverter<S> converter_;
	
	public CustomEntityConverterOrHttpFailureClosure(final HttpClient client,
		final CustomEntityConverter<S> converter) {
		super(client);
		converter_ = converter;
	}
	
	public CustomEntityConverterOrHttpFailureClosure(
		final CustomEntityConverter<S> converter) {
		this(getNewInstanceWithProxySelector(), converter);
	}

	@Override
	public S success(final HttpSuccess success) throws Exception {
		return converter_.convert(success);
	}
	
}
