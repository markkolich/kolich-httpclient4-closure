/**
 * Copyright (c) 2013 Mark S. Kolich
 * http://mark.koli.ch
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */

package com.kolich.http.async;

import static com.kolich.http.common.response.ResponseUtils.consumeQuietly;
import static org.apache.http.nio.client.methods.HttpAsyncMethods.create;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.Future;

import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.ContentDecoder;
import org.apache.http.nio.IOControl;
import org.apache.http.nio.client.HttpAsyncClient;
import org.apache.http.nio.protocol.AbstractAsyncResponseConsumer;
import org.apache.http.nio.protocol.HttpAsyncResponseConsumer;
import org.apache.http.protocol.HttpContext;

import com.kolich.http.common.HttpClient4ClosureBase;
import com.kolich.http.common.either.HttpResponseEither;
import com.kolich.http.common.either.Left;
import com.kolich.http.common.either.Right;
import com.kolich.http.common.response.HttpFailure;
import com.kolich.http.common.response.HttpSuccess;

public abstract class HttpAsyncClient4Closure<S>
	extends HttpClient4ClosureBase<Exception,Future<S>> {
				
	private final HttpAsyncClient client_;	
	
	public HttpAsyncClient4Closure(final HttpAsyncClient client) {
		client_ = client;
	}
	
	@Override
	public final HttpResponseEither<Exception,Future<S>> doit(
		final HttpRequestBase request, final HttpContext context) {
		return execute(request, context, new AbstractAsyncResponseConsumer<S>() {
			private HttpResponse response_ = null;
			private ByteBuffer buffer_ = null;
			@Override
			protected void onResponseReceived(final HttpResponse response)
				throws HttpException, IOException {
				// Seems to always be called when a response is received for
				// this request.  Feels like a good place to check for success.
				try {
					if(!check(response)) {
						failure(new HttpFailure(response, context));
					} else {
						buffer_ = ByteBuffer.allocate(8*1024);
					}
				} catch (Exception e) {
					throw new HttpException("Unexpected error occured " +
						"while checking response for success.", e);
				}
			}
			@Override
			protected void onContentReceived(final ContentDecoder decoder,
				final IOControl ioctrl) throws IOException {
				if (buffer_ == null) {
		            throw new IllegalStateException("Byte buffer is null");
		        }
		        for (;;) {
		            int bytesRead = decoder.read(buffer_);
		            if (bytesRead <= 0) {
		                break;
		            }
		        }
			}
			@Override
			protected void onEntityEnclosed(final HttpEntity entity,
				final ContentType contentType) throws IOException {
				// The entity that's passed here is the same as what's set
				// in onResponseReceived(response.getEntity()) fwiw.
				// Entering this method seems to indicate that there's a valid
				// response worth processing.
				
			}
			@Override
			protected S buildResult(final HttpContext context) throws Exception {
				S result = null;
				try {
					result = success(new HttpSuccess(response_, context));
				} catch (Exception e) {
					failure(new HttpFailure(e));
				}
				return result;
			}
			@Override
			protected void releaseResources() {
				consumeQuietly(response_);
			}
		});
	}
	
	private final HttpResponseEither<Exception,Future<S>> execute(
		final HttpRequestBase request, final HttpContext context,
		final HttpAsyncResponseConsumer<S> consumer) {
		try {
			// Before the request is "executed" give the consumer an entry
			// point into the raw request object to tweak as necessary first.
			// Usually things like "signing" the request or modifying the
			// destination host are done here.
			before(request, context);
			// Actually execute the request, get a response.
			return Right.right(client_.execute(
				// Create a new Http Async request "method".
				create(request),
				// Send in the response consumer.
				consumer,
				// Not passing any future callback, intentional.
				null));
		} catch (Exception e) {
			return Left.left(e);
		}
	}
			
	/**
	 * Called only if the request is successful.  Success is defined by
	 * the boolean state that the {@link #check} method returns.  If
	 * {@link #check} returns true, the request is considered to be
	 * successful. If it returns false, the request failed.  
	 * @param success
	 * @param context
	 * @return
	 * @throws Exception
	 */
	public abstract S success(final HttpSuccess success)
		throws Exception;
	
	/**
	 * Called only if the request is unsuccessful.  The default behavior,
	 * as implemented here, is to simply do nothing if the request failed.
	 * Consumers should override this default behavior if they need to extract
	 * more granular information about the failure, like an {@link Exception}
	 * or status code.
	 * @param failure
	 */
	public abstract void failure(final HttpFailure failure);
	
	/**
	 * Called when the asynchronous request/operation is cancelled.
	 * The default behavior, in this default implementation, is to do
	 * nothing.  However, if the consumer wishes to be notified or do
	 * something other than "nothing" when the request is cancelled,
	 * this method should be overridden in the anonymous class
	 * implementation as needed.
	 * @param request
	 * @param context
	 */
	public void cancel(final HttpRequestBase request,
		final HttpContext context) {
		// Default, do nothing.
	}
		
}
