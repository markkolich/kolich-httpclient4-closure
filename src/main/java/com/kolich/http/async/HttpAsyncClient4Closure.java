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

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.concurrent.Cancellable;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.ContentDecoder;
import org.apache.http.nio.IOControl;
import org.apache.http.nio.client.HttpAsyncClient;
import org.apache.http.nio.client.methods.HttpAsyncMethods;
import org.apache.http.nio.protocol.AbstractAsyncResponseConsumer;
import org.apache.http.protocol.HttpContext;

import com.kolich.common.functional.either.Either;
import com.kolich.common.functional.either.Left;
import com.kolich.http.common.HttpClient4ClosureBase;
import com.kolich.http.common.response.HttpFailure;
import com.kolich.http.common.response.HttpSuccess;

public abstract class HttpAsyncClient4Closure<F,S>
	extends HttpClient4ClosureBase<Future<Either<F,S>>> {
					
	private final HttpAsyncClient client_;
	
	public HttpAsyncClient4Closure(final HttpAsyncClient client) {
		client_ = client;
	}
	
	@Override
	public Future<Either<F,S>> doit(
		final HttpRequestBase request, final HttpContext context) {
		Future<Either<F,S>> result = null;
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
				new AbstractAsyncResponseConsumer<Either<F,S>>() {
					@Override
					protected void onResponseReceived(final HttpResponse response)
						throws HttpException, IOException {
						// Gosh, this feels wrong but in order to inject the
						// consumer's call to the after() method, we have to
						// double wrap the onResponseReceived() call in a
						// separate, internal try catch. Bah!
						try {
							after(response, context);
							HttpAsyncClient4Closure.this.onResponseReceived(response);
						} catch (HttpException e) {
							throw e;
						} catch (IOException e) {
							throw e;
						} catch (Exception e) {
							throw new HttpException("Unexpected exception " +
								"occurred while handling response.", e);
						}
					}
					@Override
					protected void onContentReceived(final ContentDecoder decoder,
						final IOControl ioctrl) throws IOException {
						HttpAsyncClient4Closure.this.onContentReceived(decoder, ioctrl);
					}
					@Override
					protected void onEntityEnclosed(final HttpEntity entity,
						final ContentType contentType) throws IOException {
						HttpAsyncClient4Closure.this.onEntityEnclosed(entity, contentType);
					}
					@Override
					protected Either<F,S> buildResult(
						final HttpContext context) throws Exception {
						return HttpAsyncClient4Closure.this.buildResult(context);
					}
					@Override
					protected void releaseResources() {
						HttpAsyncClient4Closure.this.releaseResources();
					}
				},
				// Internal HTTP request context.
				context,
				// Intentionally not providing a FutureCallback<T>
				null));
		} catch (Exception e) {
			// If something went wrong, create a new future that's already
			// "done" and contains an Either<F,S> where the Left error type is
			// immeaditely set to indicate failure.
			final Either<F,S> either = Left.left(failure(new HttpFailure(e)));
			result = new Future<Either<F,S>>() {
				@Override
				public boolean cancel(boolean mayInterruptIfRunning) { return false; }
				@Override
				public boolean isCancelled() { return false; }
				@Override
				public boolean isDone() { return true; }
				@Override
				public Either<F,S> get()
					throws InterruptedException, ExecutionException {
					return either;
				}
				@Override
				public Either<F,S> get(long timeout, TimeUnit unit)
					throws InterruptedException, ExecutionException, TimeoutException {
					return either;
				}
			};
		}
		return result;
	}
		
	public abstract void onResponseReceived(final HttpResponse response)
		throws IOException;
	public abstract void onContentReceived(final ContentDecoder decoder,
		final IOControl ioctrl) throws IOException;
	public abstract void onEntityEnclosed(final HttpEntity entity,
		final ContentType contentType) throws IOException;
	public abstract Either<F,S> buildResult(final HttpContext context)
		throws Exception;
	public abstract void releaseResources();
	
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
		Future<Either<F,S>>, Cancellable {		
		private final Future<Either<F,S>> future_;		
		private InternalBasicFutureWrapper(final Future<Either<F,S>> future) {
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
		public Either<F,S> get() throws InterruptedException,
			ExecutionException {
			try {
				return future_.get();
			} catch (Exception e) {
				return Left.left(failure(new HttpFailure(e)));
			}
		}
		@Override
		public Either<F,S> get(long timeout, TimeUnit unit)
			throws InterruptedException, ExecutionException, TimeoutException {
			try {
				return future_.get(timeout, unit);
			} catch (Exception e) {
				return Left.left(failure(new HttpFailure(e)));
			}
		}
	}
	
}
