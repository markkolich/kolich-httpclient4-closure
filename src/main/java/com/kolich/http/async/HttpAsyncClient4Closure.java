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

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.concurrent.Cancellable;
import org.apache.http.nio.client.HttpAsyncClient;
import org.apache.http.nio.client.methods.HttpAsyncMethods;
import org.apache.http.nio.protocol.HttpAsyncResponseConsumer;
import org.apache.http.protocol.HttpContext;

import com.kolich.http.async.futures.AlreadyDoneFuture;
import com.kolich.http.common.HttpClient4ClosureBase;
import com.kolich.http.common.either.HttpResponseEither;
import com.kolich.http.common.either.Left;
import com.kolich.http.common.response.HttpFailure;
import com.kolich.http.common.response.HttpSuccess;

public abstract class HttpAsyncClient4Closure<F,S>
	extends HttpClient4ClosureBase<Future<HttpResponseEither<F,S>>> {
					
	private final HttpAsyncClient client_;
	
	public HttpAsyncClient4Closure(final HttpAsyncClient client) {
		client_ = client;
	}
	
	@Override
	public Future<HttpResponseEither<F,S>> doit(
		final HttpRequestBase request, final HttpContext context) {
		Future<HttpResponseEither<F,S>> result = null;
		try {
			// Before the request is "executed" give the consumer an entry
			// point into the raw request object to tweak as necessary first.
			// Usually things like "signing" the request or modifying the
			// destination host are done here.
			before(request, context);
			// Go ahead and execute the asynchronous request.  Note the
			// resulting Future<T> is wrapped by an internal wrapper so
			// that any exceptions that pop-up when future.get() is called
			// can be caught gracefully and wrapped up in the
			// HttpResponseEither<F,S>.
			result = new InternalBasicFutureWrapper(client_.execute(
				// Create a fresh asynchronous request object from the
				// incoming request base.
				HttpAsyncMethods.create(request),
				// Load the asynchronous response consumer; this is the
				// "class" that will be responsible for doing the real work
				// asynchronously under-the-hood to process the response.
				getConsumer(),
				// Internal HTTP request context.
				context,
				// Intentionally not providing a FutureCallback<T>
				null));
		} catch (Exception e) {
			// If something went wrong, create a new future that's already
			// "done" and contains an Either<F,S> where the Left error type is
			// immeaditely set to indicate failure.
			result = AlreadyDoneFuture.create(Left.left(failure(new HttpFailure(e))));
		}
		return result;
	}
	
	public abstract HttpAsyncResponseConsumer<HttpResponseEither<F,S>> getConsumer();
	
	/**
	 * Called only if the request is successful.  Success is defined by
	 * the boolean state that the {@link #check} method returns.  If
	 * {@link #check} returns true, the request is considered to be
	 * successful. If it returns false, the request failed.  
	 * @param success
	 * @return
	 * @throws Exception
	 */
	public abstract S success(final HttpSuccess success)
		throws Exception;
	
	/**
	 * Called only if the request is unsuccessful.  The default behavior,
	 * as implemented here, is to do nothing if the request failed.
	 * Consumers should override this default behavior if they need to extract
	 * more granular information about the failure, like an {@link Exception}
	 * or status code.
	 * @param failure
	 */
	public F failure(final HttpFailure failure) {
		// Default, nothing.
		return null;
	}
	
	private final class InternalBasicFutureWrapper implements 
		Future<HttpResponseEither<F,S>>, Cancellable {		
		private final Future<HttpResponseEither<F,S>> future_;		
		private InternalBasicFutureWrapper(final Future<HttpResponseEither<F,S>> future) {
			future_ = future;
		}
		@Override
		public boolean cancel() {
			return future_.cancel(true);
		}
		@Override
		public boolean cancel(boolean mayInterruptIfRunning) {
			return future_.cancel(mayInterruptIfRunning);
		}
		@Override
		public boolean isCancelled() {
			return future_.isCancelled();
		}
		@Override
		public boolean isDone() {
			return future_.isDone();
		}
		@Override
		public HttpResponseEither<F,S> get() throws InterruptedException,
			ExecutionException {
			try {
				return future_.get();
			} catch (Exception e) {
				return Left.left(failure(new HttpFailure(e)));
			}
		}
		@Override
		public HttpResponseEither<F,S> get(long timeout, TimeUnit unit)
			throws InterruptedException, ExecutionException, TimeoutException {
			try {
				return future_.get(timeout, unit);
			} catch (Exception e) {
				return Left.left(failure(new HttpFailure(e)));
			}
		}
	}
	
}
