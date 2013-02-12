package com.kolich.http;

import static com.kolich.http.async.KolichDefaultHttpAsyncClient.KolichDefaultHttpAsyncClientFactory.getNewAsyncInstanceWithProxySelector;

import java.util.concurrent.Future;

import org.apache.http.nio.client.HttpAsyncClient;

import com.kolich.http.async.helpers.InMemoryStringClosure;
import com.kolich.http.common.either.HttpResponseEither;

public final class AsyncTest {

	public static void main(String[] args) throws Exception {
		
		final HttpAsyncClient client = getNewAsyncInstanceWithProxySelector("foobar");
		client.start(); // important
		
		try {
			
			HttpResponseEither<Exception,Future<HttpResponseEither<Exception,String>>> result =
				new InMemoryStringClosure(client)
					.get("http://www.google.com");
			
			if(!result.success()) {
				System.out.println("failed to queue request!");
			}
			
			while(true) {
				final Future<HttpResponseEither<Exception,String>> future = result.right();
				if(future.isDone()) {
					System.out.println("got HTML string: " +
						future.get().right().length());
				} else {
					System.out.println("not done");
				}
				Thread.sleep(1000L);
			}
			
		} finally {
			client.shutdown(); // important too
		}
		
	}

}
