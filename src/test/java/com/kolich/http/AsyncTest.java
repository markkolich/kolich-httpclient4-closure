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

	public static void main(String[] args) throws Exception {
		
		final HttpAsyncClient client = getNewAsyncInstanceWithProxySelector("foobar");
		client.start(); // important
		
		try {
			
			HttpResponseEither<Exception,Future<HttpResponse>> result =
				new HttpAsyncClient4Closure(client) {
				@Override
				public void success(final HttpSuccess success) throws Exception {
					System.out.println(Thread.currentThread().getName() +
						" -- google " + success.getStatusLine());
					Thread.sleep(20000L); // blocks the I/O dispatcher thread!!
				}
				@Override
				public void failure(final HttpFailure failure) {
					System.out.println(Thread.currentThread().getName() +
						" -- (failure) google " + failure.getStatusLine());
				}
			}.get("http://www.google.com");
			if(!result.success()) {
				System.out.println("failed to queue request!");
			}
			
			result = new HttpAsyncClient4Closure(client) {
				@Override
				public void success(final HttpSuccess success) throws Exception {
					System.out.println(Thread.currentThread().getName() +
						" -- kolich.com " + success.getStatusLine());
					Thread.sleep(20000L); // blocks the I/O dispatcher thread!!
				}
				@Override
				public void failure(final HttpFailure failure) {
					System.out.println(Thread.currentThread().getName() +
						" -- (failure) kolich.com " + failure.getStatusLine());
				}
			}.get("http://kolich.com");
			if(!result.success()) {
				System.out.println("failed to queue request!");
			}
			
			result = new HttpAsyncClient4Closure(client) {
				@Override
				public void success(final HttpSuccess success) throws Exception {
					System.out.println(Thread.currentThread().getName() +
						" -- vmware.com " + success.getStatusLine());
					Thread.sleep(20000L); // blocks the I/O dispatcher thread!!
				}
				@Override
				public void failure(final HttpFailure failure) {
					System.out.println(Thread.currentThread().getName() +
						" -- (failure) vmware.com " + failure.getStatusLine());
				}
			}.get("http://vmware.com");
			if(!result.success()) {
				System.out.println("failed to queue request!");
			}
			
			result = new HttpAsyncClient4Closure(client) {
				@Override
				public void success(final HttpSuccess success) throws Exception {
					System.out.println(Thread.currentThread().getName() +
						" -- sencha.com " + success.getStatusLine());
					Thread.sleep(20000L); // blocks the I/O dispatcher thread!!
				}
				@Override
				public void failure(final HttpFailure failure) {
					System.out.println(Thread.currentThread().getName() +
						" -- (failure) sencha.com " + failure.getStatusLine());
				}
			}.get("http://sencha.com");
			if(!result.success()) {
				System.out.println("failed to queue request!");
			}
			
			// So in theory, this should fail to queue cuz all I/O
			// dispathcer threads are busy (blocked in thread.sleep() in
			// the success methods above).
			result = new HttpAsyncClient4Closure(client) {
				@Override
				public void success(final HttpSuccess success) throws Exception {
					System.out.println(Thread.currentThread().getName() +
						" -- (succesS) PUT kolich.com " + success.getStatusLine());
				}
				@Override
				public void failure(final HttpFailure failure) {
					System.out.println(Thread.currentThread().getName() +
						" -- (failure) PUT kolich.com " + failure.getStatusLine());
				}
			}.put("http://kolich.com");
			if(!result.success()) {
				System.out.println("failed to queue PUT request!");
			}
			
			Thread.sleep(60000L); // Wait for a bit to let the requests finish.
			
		} finally {
			client.shutdown(); // important too
		}
		
	}

}
