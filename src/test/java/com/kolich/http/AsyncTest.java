package com.kolich.http;

import static com.kolich.common.DefaultCharacterEncoding.UTF_8;
import static com.kolich.http.async.KolichDefaultHttpAsyncClient.KolichHttpAsyncClientFactory.getNewAsyncInstanceWithProxySelector;

import java.io.IOException;
import java.util.concurrent.Future;

import org.apache.http.ContentTooLongException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.ContentDecoder;
import org.apache.http.nio.IOControl;
import org.apache.http.nio.client.HttpAsyncClient;
import org.apache.http.nio.entity.ContentBufferEntity;
import org.apache.http.nio.util.HeapByteBufferAllocator;
import org.apache.http.nio.util.SimpleInputBuffer;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import com.kolich.http.async.HttpAsyncClient4Closure;
import com.kolich.http.async.helpers.InMemoryAsyncStringClosure;
import com.kolich.http.common.either.HttpResponseEither;
import com.kolich.http.common.either.Left;
import com.kolich.http.common.either.Right;
import com.kolich.http.common.response.HttpFailure;
import com.kolich.http.common.response.HttpSuccess;

public final class AsyncTest {

	public static void main(String[] args) throws Exception {
		
		final HttpAsyncClient client = getNewAsyncInstanceWithProxySelector("foobar");
		client.start(); // important
		
		try {
			
			final Future<HttpResponseEither<HttpFailure,String>> request =
				new HttpAsyncClient4Closure<HttpFailure,String>(client) {
					private HttpResponse response_;
					private SimpleInputBuffer buffer_;
					@Override
					public void onResponseReceived(final HttpResponse response)
						throws IOException {
						response_ = response;
					}
					@Override
					public void onContentReceived(final ContentDecoder decoder,
						final IOControl ioctrl) throws IOException {
						if (buffer_ == null) {
				            throw new IllegalStateException("Content buffer " +
				            	"is null.");
				        }
				        buffer_.consumeContent(decoder);
					}
					@Override
					public void onEntityEnclosed(final HttpEntity entity,
						final ContentType contentType) throws IOException {
						long len = entity.getContentLength();
				        if (len > Integer.MAX_VALUE) {
				            throw new ContentTooLongException("Entity content " +
				            	"is too long: " + len);
				        } else if (len < 0) {
				            len = 4096;
				        }
				        buffer_ = new SimpleInputBuffer((int) len,
				        	new HeapByteBufferAllocator());
				        response_.setEntity(new ContentBufferEntity(entity,
				        	buffer_));
					}
					@Override
					public void releaseResources() {
						response_ = null;
						buffer_ = null;
					}
					
					@Override
					public HttpResponseEither<HttpFailure,String> buildResult(
						final HttpContext context) throws Exception {
						HttpResponseEither<HttpFailure,String> result = null;
						try {
							if(check(response_, context)) {
								result = Right.right(success(new HttpSuccess(
									response_, context)));
							} else {
								result = Left.left(failure(new HttpFailure(
									response_, context)));
							}
						} catch (Exception e) {
							result = Left.left(failure(new HttpFailure(e)));
						}
						return result;
					}
					@Override
					public String success(final HttpSuccess success) throws Exception {
						return EntityUtils.toString(success.getEntity(), UTF_8);
					}
			}.get("http://www.example.com");
			
			final Future<HttpResponseEither<HttpFailure,String>> future =
				new InMemoryAsyncStringClosure(client) {
				@Override
				public void before(final HttpRequestBase request) {
					request.addHeader("Authorization", "super-secret-password");
				}
			}.get("http://www.google.com");
						
			while(true) {
				if(future.isDone() && request.isDone()) {
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
