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

import static java.net.URI.create;
import static org.apache.http.HttpStatus.SC_BAD_REQUEST;
import static org.apache.http.util.EntityUtils.consume;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import com.kolich.http.either.HttpResponseEither;
import com.kolich.http.either.Left;
import com.kolich.http.either.Right;
import com.kolich.http.response.HttpFailure;
import com.kolich.http.response.HttpSuccess;

public abstract class HttpClient4Closure<F,S> {
				
	private final HttpClient client_;
	
	public HttpClient4Closure(final HttpClient client) {
		client_ = client;
	}
	
	public HttpResponseEither<F,S> head(final String url) {
		return head(create(url));
	}
	
	public HttpResponseEither<F,S> head(final URI uri) {
		return head(new HttpHead(uri));
	}
	
	public HttpResponseEither<F,S> head(final HttpHead head) {
		return head(head, null);
	}
	
	public HttpResponseEither<F,S> head(final HttpHead head,
		final HttpContext context) {
		return request(head, context);
	}
	
	public HttpResponseEither<F,S> get(final String url) {
		return get(create(url));
	}
	
	public HttpResponseEither<F,S> get(final URI uri) {
		return get(new HttpGet(uri));
	}
	
	public HttpResponseEither<F,S> get(final HttpGet get) {
		return get(get, null);
	}
	
	public HttpResponseEither<F,S> get(final HttpGet get,
		final HttpContext context) {
		return request(get, context);
	}
	
	public HttpResponseEither<F,S> post(final String url) {
		return post(create(url));
	}
	
	public HttpResponseEither<F,S> post(final URI uri) {
		return post(new HttpPost(uri), null, null);
	}
	
	public HttpResponseEither<F,S> post(final HttpPost post) {
		return post(post, null, null);
	}
	
	public HttpResponseEither<F,S> post(final HttpPost post,
		final byte[] body, final String contentType) {
		return post(post,
			(body != null) ? new ByteArrayInputStream(body) : null,
			(body != null) ? (long)body.length : 0L,
			contentType);
	}
	
	public HttpResponseEither<F,S> post(final HttpPost post,
		final InputStream is, final long length, final String contentType) {
		return post(post, is, length, contentType, null);
	}
	
	public HttpResponseEither<F,S> post(final HttpPost post,
		final InputStream is, final long length, final String contentType,
		final HttpContext context) {
		if(is != null) {
			final InputStreamEntity entity = new InputStreamEntity(is, length);			
			if(contentType != null) {
				entity.setContentType(contentType);
			}
			post.setEntity(entity);
		}
		return request(post, context);
	}
	
	public HttpResponseEither<F,S> put(final String url) {
		return put(create(url));
	}
	
	public HttpResponseEither<F,S> put(final URI uri) {
		return put(new HttpPut(uri), null);
	}
	
	public HttpResponseEither<F,S> put(final HttpPut put) {
		return put(put, null);
	}
	
	public HttpResponseEither<F,S> put(final HttpPut put,
		final byte[] body) {
		return put(put, body, null, new BasicHttpContext());
	}
	
	public HttpResponseEither<F,S> put(final HttpPut put,
		final byte[] body, final String contentType, final HttpContext context) {
		return put(put,
			(body != null) ? new ByteArrayInputStream(body) : null,
			(body != null) ? (long)body.length : 0L,
			contentType);
	}
	
	public HttpResponseEither<F,S> put(final HttpPut put,
		final InputStream is, final long length, final String contentType) {
		return put(put, is, length, contentType, null);
	}
	
	public HttpResponseEither<F,S> put(final HttpPut put,
		final InputStream is, final long length, final String contentType,
		final HttpContext context) {
		if(is != null) {
			final InputStreamEntity entity = new InputStreamEntity(is, length);			
			if(contentType != null) {
				entity.setContentType(contentType);
			}
			put.setEntity(entity);
		}
		return request(put, context);
	}
	
	public HttpResponseEither<F,S> delete(final String url) {
		return delete(create(url));
	}
	
	public HttpResponseEither<F,S> delete(final URI uri) {
		return delete(new HttpDelete(uri));
	}
	
	public HttpResponseEither<F,S> delete(final HttpDelete delete) {
		return delete(delete, null);
	}
	
	public HttpResponseEither<F,S> delete(final HttpDelete delete,
		final HttpContext context) {
		return request(delete, context);
	}
	
	public HttpResponseEither<F,S> request(final HttpRequestBase request) {
		return request(request, null);
	}
	
	public final HttpResponseEither<F,S> request(final HttpRequestBase request,
		final HttpContext context) {
		return doit(request, (context == null) ?
			new BasicHttpContext() : context);
	}
	
	private final HttpResponseEither<F,S> doit(final HttpRequestBase request,
		final HttpContext context) {
		HttpResponseEither<F,S> result = null;
		// Any failures/exceptions encountered during request execution
		// (in a call to execute) are wrapped up as a Left() and are delt
		// with in the failure path below.
		final HttpResponseEither<HttpFailure,HttpSuccess> response =
			execute(request, context);
		try {
			if(response.success()) {
				result = Right.right(success(((Right<HttpFailure,HttpSuccess>)
					response).right_));
			} else {
				result = Left.left(failure(((Left<HttpFailure,HttpSuccess>)
					response).left_));
			}
		} catch (Exception e) {
			// Wrap up any failures/exceptions that might have occurred
			// while processing the response.
			result = Left.left(failure(new HttpFailure(e)));
		} finally {
			if(response.success()) {
				consumeQuietly(((Right<HttpFailure,HttpSuccess>)
					response).right_.getResponse());
			} else {
				consumeQuietly(((Left<HttpFailure,HttpSuccess>)
					response).left_.getResponse());
			}
		}
		return result;
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
			// Immediately after execution, only if the request was executed.
			after(response, context);
			// Check if the response was "successful".  The definition of
			// success is arbitrary based on what's defined in the check()
			// method.  The default success check is simply checking the
			// HTTP status code and if it's less than 400 (Bad Request) then
			// it's considered "good".  If the user wants evaluate this
			// response against some custom criteria, they should override
			// this check() method.
			if(check(response, context)) {
				return Right.right(new HttpSuccess(response, context));
			} else {
				return Left.left(new HttpFailure(response, context));
			}
		} catch (Exception e) {
			// Something went wrong with the request, abort it,
			// return failure.
			request.abort();
			return Left.left(new HttpFailure(e, response, context));
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
	 * Called immeaditely after request execution, but before the response
	 * is checked for "success" via {@link #check(HttpResponse)}.  Is only called
	 * if there were no exceptions that would have resulted from
	 * attempting to execute the request.
	 * @param response
	 * @param context
	 * @throws Exception
	 */
	public void after(final HttpResponse response, final HttpContext context)
		throws Exception {
		// Default, do nothing.
	}
	
	/**
	 * Called immediately after request execution has completed.  Checks if
	 * the response was "successful".  The definition of success is arbitrary
	 * based on what's defined in this method.  The default success check is
	 * simply checking the HTTP status code and if it's less than 400
	 * (Bad Request) then it's considered "good".  If the user wants evaluate
	 * a response against some custom criteria, they should override
	 * this method and implement their own logic in their extending class.
	 * @param response
	 * @param context
	 * @return
	 */
	public boolean check(final HttpResponse response, final HttpContext context)
		throws Exception {
		return check(response);
	}
	public boolean check(final HttpResponse response) throws Exception {
		return (response.getStatusLine().getStatusCode() < SC_BAD_REQUEST);
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
	 * as implemented here, is to simply return null if the request failed.
	 * Consumers should override this default behavior if they need to extract
	 * more granular information about the failure, like an {@link Exception}
	 * or status code.
	 * @param failure
	 * @return null by default, override this if you want to return something else
	 */
	public F failure(final HttpFailure failure) {
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
	
}
