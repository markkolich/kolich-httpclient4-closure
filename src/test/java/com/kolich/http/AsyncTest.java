package com.kolich.http;

import static com.kolich.http.async.KolichDefaultHttpAsyncClient.KolichDefaultHttpAsyncClientFactory.getNewAsyncInstanceWithProxySelector;

import java.util.concurrent.Future;

import org.apache.http.HttpResponse;
import org.apache.http.nio.client.HttpAsyncClient;

import com.kolich.http.async.HttpAsyncClient4Closure;
import com.kolich.http.common.either.HttpResponseEither;
import com.kolich.http.common.response.HttpFailure;
import com.kolich.http.common.response.HttpSuccess;

public final class AsyncTest {

	public static void main(String[] args) {
		
		final HttpAsyncClient client = getNewAsyncInstanceWithProxySelector("foobar");
		
		final HttpResponseEither<Exception,Future<HttpResponse>> result =
			new HttpAsyncClient4Closure(client) {
			@Override
			public void success(final HttpSuccess success) throws Exception {
				System.out.println("");
			}
			@Override
			public void failure(final HttpFailure failure) {
				
			}			
		}.get("http://www.google.com");

	}

}
