package com.kolich.http;

import static com.kolich.common.DefaultCharacterEncoding.UTF_8;
import static com.kolich.http.async.KolichDefaultHttpAsyncClient.KolichDefaultHttpAsyncClientFactory.getNewAsyncInstanceWithProxySelector;

import java.util.concurrent.Future;

import org.apache.http.nio.client.HttpAsyncClient;
import org.apache.http.util.EntityUtils;

import com.kolich.http.async.HttpAsyncClient4Closure;
import com.kolich.http.common.either.HttpResponseEither;
import com.kolich.http.common.response.HttpSuccess;

public final class AsyncTest {

	public static void main(String[] args) throws Exception {
		
		final HttpAsyncClient client = getNewAsyncInstanceWithProxySelector("foobar");
		client.start(); // important
		
		try {
			
			HttpResponseEither<Exception,Future<HttpResponseEither<Exception,String>>> result =
				new HttpAsyncClient4Closure<Exception,String>(client) {
					@Override
					public String success(final HttpSuccess success) throws Exception {
						System.out.println("==== in success ====");
						Thread.sleep(5000L);
						return EntityUtils.toString(success.getEntity(), UTF_8);
					}
			}.get("http://www.google.com");
			if(!result.success()) {
				System.out.println("failed to queue request!");
			}
			
			while(true) {
				if(result == null) {
					System.out.println("null");
				} else {
					if(result.right() == null) {
						System.out.println("right is null");
					} else {
						System.out.println("right has something!");
					}
				}
				Thread.sleep(1000L);
			}
			
		} finally {
			client.shutdown(); // important too
		}
		
	}

}
