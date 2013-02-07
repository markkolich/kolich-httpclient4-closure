package com.kolich.http;

import static com.kolich.http.async.KolichDefaultHttpAsyncClient.KolichDefaultHttpAsyncClientFactory.getNewAsyncInstanceNoProxySelector;

import java.util.concurrent.Future;

import org.apache.http.HttpResponse;
import org.apache.http.nio.client.HttpAsyncClient;

import com.kolich.http.async.HttpAsyncClient4Closure;
import com.kolich.http.common.either.HttpResponseEither;
import com.kolich.http.common.response.HttpFailure;
import com.kolich.http.common.response.HttpSuccess;

public final class AsyncTest {

	public static void main(String[] args) throws Exception {
		
		final HttpAsyncClient client = getNewAsyncInstanceNoProxySelector("foobar");
		
		final HttpResponseEither<Exception,Future<HttpResponse>> result =
			new HttpAsyncClient4Closure(client) {
			@Override
			public void success(final HttpSuccess success) throws Exception {
				System.out.println(success.getStatusCode());
			}
			@Override
			public void failure(final HttpFailure failure) {
				System.out.println(failure.getStatusCode());
			}
		}.get("http://www.google.com");
		
		if(result.success()) {
			System.out.println("worked...");
			result.right().get();
		} else {
			System.out.println("failed");
		}

	}

}
