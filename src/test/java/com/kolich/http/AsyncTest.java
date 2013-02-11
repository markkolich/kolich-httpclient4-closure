package com.kolich.http;

import static com.kolich.http.async.KolichDefaultHttpAsyncClient.KolichDefaultHttpAsyncClientFactory.getNewAsyncInstanceWithProxySelector;

import java.util.concurrent.Future;

import org.apache.http.nio.client.HttpAsyncClient;

import com.kolich.http.async.HttpAsyncClient4Closure;
import com.kolich.http.common.either.HttpResponseEither;
import com.kolich.http.common.response.HttpFailure;
import com.kolich.http.common.response.HttpSuccess;

public final class AsyncTest {

	public static void main(String[] args) throws Exception {
		
		final HttpAsyncClient client = getNewAsyncInstanceWithProxySelector("foobar");
		client.start(); // important
		
		try {
			
			HttpResponseEither<Exception,Future<Void>> result =
				new HttpAsyncClient4Closure<Void>(client) {
				@Override
				public Void success(final HttpSuccess success) throws Exception {
					System.out.println("Oh hai google!");
					return null;
				}
				@Override
				public void failure(final HttpFailure failure) {
					System.out.println("Failed miserably");
				}
			}.get("http://www.google.com");
			if(!result.success()) {
				System.out.println("failed to queue request!");
			}
			
			Thread.sleep(60000L); // Wait for a bit to let the requests finish.
			
		} finally {
			client.shutdown(); // important too
		}
		
	}

}
