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

import java.util.concurrent.Future;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.nio.client.HttpAsyncClient;
import org.apache.http.protocol.HttpContext;

import com.kolich.http.common.HttpClient4ClosureBase;
import com.kolich.http.common.either.HttpResponseEither;
import com.kolich.http.common.either.Left;
import com.kolich.http.common.either.Right;
import com.kolich.http.common.response.HttpFailure;
import com.kolich.http.common.response.HttpSuccess;

public abstract class HttpAsyncClient4Closure
	extends HttpClient4ClosureBase<Exception,Future<HttpResponse>> {
				
	protected final HttpAsyncClient client_;
	
	public HttpAsyncClient4Closure(final HttpAsyncClient client) {
		client_ = client;
	}
	
	@Override
	public final HttpResponseEither<Exception,Future<HttpResponse>> doit(
		final HttpRequestBase request, final HttpContext context) {
		try {
			// Before the request is "executed" give the consumer an entry
			// point into the raw request object to tweak as necessary first.
			// Usually things like "signing" the request or modifying the
			// destination host are done here.
			before(request, context);
			// Actually execute the request, get a response.
			return Right.right(client_.execute(request, context,
				new FutureCallback<HttpResponse>() {
				@Override
				public void completed(final HttpResponse response) {
					try {
						// Immediately after execution, only if the request
						// was executed.
						after(response, context);
						// Check if the response was "successful".  The
						// definition of success is arbitrary based on what's
						// defined in the check() method.  The default success
						// check is simply checking the HTTP status code and if
						// it's less than 400 (Bad Request) then it's considered
						// "good".  If the user wants evaluate this response
						// against some custom criteria, they should override
						// this check() method.
						if(check(response, context)) {
							success(new HttpSuccess(response, context));
						} else {
							failure(new HttpFailure(response, context));
						}
					} catch (Exception e) {
						failure(new HttpFailure(e));
					} finally {
						consumeQuietly(response);
					}
				}
				@Override
				public void failed(final Exception e) {
					failure(new HttpFailure(e));
				}
				@Override
				public void cancelled() {
					cancel(request, context);
				}
			}));
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
	public abstract void success(final HttpSuccess success)
		throws Exception;

	/**
	 * Called only if the request is unsuccessful.  The default behavior,
	 * as implemented here, is to simply do nothing if the request failed.
	 * Consumers should override this default behavior if they need to extract
	 * more granular information about the failure, like an {@link Exception}
	 * or status code.
	 * @param failure
	 */
	public void failure(final HttpFailure failure) {
		// Default, do nothing.
	}
	
	public void cancel(final HttpRequestBase request, final HttpContext context) {
		// Default, do nothing.
	}
		
}
