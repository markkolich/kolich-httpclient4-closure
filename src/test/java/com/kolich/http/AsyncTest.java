package com.kolich.http;

import static com.kolich.http.async.KolichDefaultHttpAsyncClient.KolichHttpAsyncClientFactory.getNewAsyncInstanceWithProxySelector;

import java.util.concurrent.Future;

import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.nio.client.HttpAsyncClient;

import com.kolich.http.async.helpers.InMemoryAsyncStringClosure;
import com.kolich.http.common.either.HttpResponseEither;
import com.kolich.http.common.response.HttpFailure;

public final class AsyncTest {

	public static void main(String[] args) throws Exception {
		
		final HttpAsyncClient client = getNewAsyncInstanceWithProxySelector("foobar");
		client.start(); // important
		
		try {
			
			final Future<HttpResponseEither<HttpFailure,String>> future =
				new InMemoryAsyncStringClosure(client) {
				@Override
				public void before(final HttpRequestBase request) {
					request.addHeader("Authorization", "super-secret-password");
				}
			}.get("http://www.google.com");
						
			while(true) {
				if(future.isDone()) {
					final HttpResponseEither<HttpFailure,String> either = future.get();
					if(either.success()) {
						System.out.println("Success!.. has " +
							either.right().length() + " long String.");
					} else {
						System.out.println("Oops, failed: " + either.left().getCause());
					}
				} else {
					System.out.println("not done yet...");
				}
				Thread.sleep(1000L);
			}
			
		} finally {
			client.shutdown(); // important too
		}
		
	}

}
