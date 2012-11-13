/**
 * Copyright (c) 2012 Mark S. Kolich
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

package com.kolich.http;

import static org.apache.http.HttpStatus.SC_BAD_REQUEST;
import static org.apache.http.util.EntityUtils.consume;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import com.kolich.http.exceptions.HttpClientClosureException;

public abstract class HttpClient4Closure<F,S> {
				
	private final HttpClient client_;
	
	public HttpClient4Closure(final HttpClient client) {
		client_ = client;
	}
	
	public final HttpResponseEither<F,S> get(final String url) {
		try {
			return get(new URL(url));
		} catch (MalformedURLException e) {
			throw new HttpClientClosureException(e);
		}
	}
	
	public final HttpResponseEither<F,S> get(final URL url) {
		try {
			return get(new HttpGet(url.toURI()));
		} catch (URISyntaxException e) {
			throw new HttpClientClosureException(e);
		}
	}
	
	public final HttpResponseEither<F,S> get(final HttpGet get) {
		return get(get, null);
	}
	
	public final HttpResponseEither<F,S> get(final HttpGet get,
		final HttpContext context) {
		return request(get, (context == null) ?
			new BasicHttpContext() : context);
	}
	
	public final HttpResponseEither<F,S> post(final String url) {
		try {
			return post(new URL(url));
		} catch (MalformedURLException e) {
			throw new HttpClientClosureException(e);
		}
	}
	
	public final HttpResponseEither<F,S> post(final URL url) {
		try {
			return post(new HttpPost(url.toURI()), null, null);
		} catch (URISyntaxException e) {
			throw new HttpClientClosureException(e);
		}
	}
	
	public final HttpResponseEither<F,S> post(final HttpPost post,
		final byte[] body, final String contentType) {
		return post(post,
			(body != null) ? new ByteArrayInputStream(body) : null,
			(body != null) ? (long)body.length : -1L,
			contentType);
	}
	
	public final HttpResponseEither<F,S> post(final HttpPost post,
		final InputStream is, final long length, final String contentType) {
		return post(post, is, length, contentType, null);
	}
	
	public final HttpResponseEither<F,S> post(final HttpPost post,
		final InputStream is, final long length, final String contentType,
		final HttpContext context) {
		if(is != null) {
			final InputStreamEntity entity = new InputStreamEntity(is, length);			
			if(contentType != null) {
				entity.setContentType(contentType);
			}
			post.setEntity(entity);
		}
		return request(post, (context == null) ?
			new BasicHttpContext() : context);
	}
	
	public final HttpResponseEither<F,S> put(final String url) {
		try {
			return put(new URL(url));
		} catch (MalformedURLException e) {
			throw new HttpClientClosureException(e);
		}
	}
	
	public final HttpResponseEither<F,S> put(final URL url) {
		try {
			return put(new HttpPut(url.toURI()), null);
		} catch (URISyntaxException e) {
			throw new HttpClientClosureException(e);
		}
	}
	
	public final HttpResponseEither<F,S> put(final HttpPut put,
		final byte[] body) {
		return put(put, body, null, new BasicHttpContext());
	}
	
	public final HttpResponseEither<F,S> put(final HttpPut put,
		final byte[] body, final String contentType, final HttpContext context) {
		return put(put,
			(body != null) ? new ByteArrayInputStream(body) : null,
			(body != null) ? (long)body.length : -1L,
			contentType);
	}
	
	public final HttpResponseEither<F,S> put(final HttpPut put,
		final InputStream is, final long length, final String contentType) {
		return put(put, is, length, contentType, null);
	}
	
	public final HttpResponseEither<F,S> put(final HttpPut put,
		final InputStream is, final long length, final String contentType,
		final HttpContext context) {
		if(is != null) {
			final InputStreamEntity entity = new InputStreamEntity(is, length);			
			if(contentType != null) {
				entity.setContentType(contentType);
			}
			put.setEntity(entity);
		}
		return request(put, (context == null) ?
			new BasicHttpContext() : context);
	}
	
	public final HttpResponseEither<F,S> delete(final String url) {
		try {
			return delete(new URL(url));
		} catch (MalformedURLException e) {
			throw new HttpClientClosureException(e);
		}
	}
	
	public final HttpResponseEither<F,S> delete(final URL url) {
		try {
			return delete(new HttpDelete(url.toURI()));
		} catch (URISyntaxException e) {
			throw new HttpClientClosureException(e);
		}
	}
	
	public final HttpResponseEither<F,S> delete(final HttpDelete delete) {
		return delete(delete, null);
	}
	
	public final HttpResponseEither<F,S> delete(final HttpDelete delete,
		final HttpContext context) {
		return request(delete, (context == null) ?
			new BasicHttpContext() : context);
	}
	
	public HttpResponseEither<F,S> request(final HttpRequestBase request,
		final HttpContext context) {
		return doit(request, context);
	}
	
	private final HttpResponseEither<F,S> doit(final HttpRequestBase request,
		final HttpContext context) {		
		final HttpResponseEither<HttpFailure,HttpSuccess> response =
			execute(request, context);
		try {
			if(response.success()) {
				return Right.right(success(((Right<HttpFailure,HttpSuccess>)
					response).right_, context));
			} else {
				return Left.left(failure(((Left<HttpFailure,HttpSuccess>)
					response).left_, context));
			}
		} catch (Exception e) {
			throw new HttpClientClosureException(e);
		} finally {
			if(response.success()) {
				consumeQuietly(((Right<HttpFailure,HttpSuccess>)response)
					.right_.getResponse());
			} else {
				consumeQuietly(((Left<HttpFailure,HttpSuccess>)response)
					.left_.getResponse());
			}
		}
	}
	
	private final HttpResponseEither<HttpFailure,HttpSuccess> execute(
		final HttpRequestBase request, final HttpContext context) {
		HttpResponse response = null;
		try {
			// Before the request is "executed" give the consumer an entry
			// point into the raw request object to tweak as necessary first.
			// Usually things like "signing" the request or modifying the
			// destination host are done here.
			before(request, context);
			// Actually execute the request, get a response.
			response = client_.execute(request, context);
			// Check if the response was "successful".  The definition of
			// success is arbitrary based on what's defined in the check()
			// method.  The default success check is simply checking the
			// HTTP status code and if it's less than 400 (Bad Request) then
			// it's considered "good".  If the user wants evaluate this
			// response against some custom criteria, they should override
			// this check() method.
			if(check(response, context)) {
				return Right.right(new HttpSuccess(response));
			} else {
				return Left.left(new HttpFailure(null, response));
			}
		} catch (Exception e) {
			// Something went wrong with the request, abort it,
			// return failure.
			request.abort();
			return Left.left(new HttpFailure(e, response));
		}
	}

	/**
	 * Called before the request is executed.  The final {@link HttpRequestBase}
	 * is passed as the only argument such that the consumer can tweak or
	 * modify the outgoing request as needed before execution.
	 * @param request
	 * @throws Exception
	 */
	public void before(final HttpRequestBase request, final HttpContext context)
		throws Exception {
		before(request);
	}
	public void before(final HttpRequestBase request) throws Exception {
		// Default, do nothing.
	}
	
	/**
	 * Called immediately after request execution has completed.  Checks if
	 * the response was "successful".  The definition of success is arbitrary
	 * based on what's defined in this method.  The default success check is
	 * simply checking the HTTP status code and if it's less than 400
	 * (Bad Request) then it's considered "good".  If the user wants evaluate
	 * this response against some custom criteria, they should override
	 * this method and implement their own logic in their extending class.
	 * @param response
	 * @param context
	 * @return
	 */
	public boolean check(final HttpResponse response, final HttpContext context)
		throws Exception {
		return (response.getStatusLine().getStatusCode() < SC_BAD_REQUEST);
	}
		
	/**
	 * Called only if the request is successful.
	 * @param success
	 * @return
	 * @throws Exception
	 */
	public abstract S success(final HttpSuccess success, final HttpContext context)
		throws Exception;
		
	/**
	 * Called only if the request is unsuccessful.  The default behavior,
	 * as implemented here, is to simply return null if the request failed.
	 * Consumers should override this default behavior if they need to extract
	 * more granular information about the failure, like an {@link Exception}
	 * or status code.
	 * @param failure
	 * @return null by default, override this if you want to return something else
	 * @throws Exception
	 */
	public F failure(final HttpFailure failure, final HttpContext context)
		throws Exception {
		return failure(failure);
	}
	public F failure(final HttpFailure failure) throws Exception {
		return null; // Default, return null on failure.
	}
	
	public static final void consumeQuietly(final HttpEntity entity) {
		try {
			consume(entity);
		} catch (Exception e) {}
	}
	
	/**
	 * Quietly closes any {@link HttpEntity} in the provided
	 * {@link HttpResponse}, suppressing any exceptions. Ensures that
	 * the entity content is fully consumed and the content stream, if exists,
	 * is closed.
	 */
	public static final void consumeQuietly(final HttpResponse response) {
		if(response != null) {
			consumeQuietly(response.getEntity());
		}
	}
	
	public static abstract class HttpClientClosureResponse {
		private final HttpResponse response_;
		public HttpClientClosureResponse(HttpResponse response) {
			response_ = response;
		}
		public final HttpResponse getResponse() {
			return response_;
		}
	}
	public static final class HttpFailure extends HttpClientClosureResponse {		
		private final Exception cause_;
		public HttpFailure(Exception cause, HttpResponse response) {
			super(response);
			cause_ = cause;
		}
		public HttpFailure(HttpResponse response) {
			this(null, response);
		}
		public Exception getCause() {
			return cause_;
		}
	}
	public static final class HttpSuccess extends HttpClientClosureResponse {
		public HttpSuccess(HttpResponse response) {
			super(response);
		}
	}
	
	public interface HttpResponseEither<F,S> {
		public abstract boolean success();
		public abstract F left();
		public abstract S right();
	}
	
	private static final class Left<F,S> implements HttpResponseEither<F,S> {
		public final F left_;
		private Left(final F left) {
			left_ = left;
		}
		@Override
		public boolean success() {
			return false;
		}		
		@Override
		public F left() {
			return left_;
		}
		@Override
		public S right() {
			return null;
		}
		private static final <F,S> HttpResponseEither<F,S> left(final F left) {
			return new Left<F,S>(left);
		}
	}

	private static final class Right<F,S> implements HttpResponseEither<F,S> {
		public final S right_;
		private Right(final S right) {
			right_ = right;
		}
		@Override
		public boolean success() {
			return true;
		}
		@Override
		public F left() {
			return null;
		}
		@Override
		public S right() {
			return right_;
		}
		private static final <F,S> HttpResponseEither<F,S> right(final S right) {
			return new Right<F,S>(right);
		}
	}
	
}
