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

import static com.kolich.http.response.ResponseUtils.consumeQuietly;
import static java.net.URI.create;
import static org.apache.http.HttpStatus.SC_BAD_REQUEST;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.util.concurrent.Future;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.nio.client.HttpAsyncClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import com.kolich.http.either.HttpResponseEither;
import com.kolich.http.either.Left;
import com.kolich.http.either.Right;
import com.kolich.http.response.HttpFailure;
import com.kolich.http.response.HttpSuccess;

public abstract class HttpAsyncClient4Closure {
				
	private final HttpAsyncClient client_;
	
	public HttpAsyncClient4Closure(final HttpAsyncClient client) {
		client_ = client;
	}
	
	public HttpResponseEither<Exception,Future<HttpResponse>> head(
		final String url) {
		return head(create(url));
	}
	
	public HttpResponseEither<Exception	,Future<HttpResponse>> head(
		final URI uri) {
		return head(new HttpHead(uri));
	}
	
	public HttpResponseEither<Exception,Future<HttpResponse>> head(
		final HttpHead head) {
		return head(head, null);
	}
	
	public HttpResponseEither<Exception,Future<HttpResponse>> head(
		final HttpHead head, final HttpContext context) {
		return request(head, context);
	}
	
	public HttpResponseEither<Exception,Future<HttpResponse>> get(
		final String url) {
		return get(create(url));
	}
	
	public HttpResponseEither<Exception,Future<HttpResponse>> get(
		final URI uri) {
		return get(new HttpGet(uri));
	}
	
	public HttpResponseEither<Exception,Future<HttpResponse>> get(
		final HttpGet get) {
		return get(get, null);
	}
	
	public HttpResponseEither<Exception,Future<HttpResponse>> get(
		final HttpGet get, final HttpContext context) {
		return request(get, context);
	}
	
	public HttpResponseEither<Exception,Future<HttpResponse>> post(
		final String url) {
		return post(create(url));
	}
	
	public HttpResponseEither<Exception,Future<HttpResponse>> post(
		final URI uri) {
		return post(new HttpPost(uri), null, null);
	}
	
	public HttpResponseEither<Exception,Future<HttpResponse>> post(
		final HttpPost post) {
		return post(post, null, null);
	}
	
	public HttpResponseEither<Exception,Future<HttpResponse>> post(
		final HttpPost post, final byte[] body, final String contentType) {
		return post(post,
			(body != null) ? new ByteArrayInputStream(body) : null,
			(body != null) ? (long)body.length : 0L,
			contentType);
	}
	
	public HttpResponseEither<Exception,Future<HttpResponse>> post(
		final HttpPost post, final InputStream is, final long length,
		final String contentType) {
		return post(post, is, length, contentType, null);
	}
	
	public HttpResponseEither<Exception,Future<HttpResponse>> post(
		final HttpPost post, final InputStream is, final long length,
		final String contentType, final HttpContext context) {
		if(is != null) {
			final InputStreamEntity entity = new InputStreamEntity(is, length);			
			if(contentType != null) {
				entity.setContentType(contentType);
			}
			post.setEntity(entity);
		}
		return request(post, context);
	}
	
	public HttpResponseEither<Exception,Future<HttpResponse>> put(
		final String url) {
		return put(create(url));
	}
	
	public HttpResponseEither<Exception,Future<HttpResponse>> put(
		final URI uri) {
		return put(new HttpPut(uri), null);
	}
	
	public HttpResponseEither<Exception,Future<HttpResponse>> put(
		final HttpPut put) {
		return put(put, null);
	}
	
	public HttpResponseEither<Exception,Future<HttpResponse>> put(
		final HttpPut put, final byte[] body) {
		return put(put, body, null, new BasicHttpContext());
	}
	
	public HttpResponseEither<Exception,Future<HttpResponse>> put(
		final HttpPut put, final byte[] body, final String contentType,
		final HttpContext context) {
		return put(put,
			(body != null) ? new ByteArrayInputStream(body) : null,
			(body != null) ? (long)body.length : 0L,
			contentType);
	}
	
	public HttpResponseEither<Exception,Future<HttpResponse>> put(
		final HttpPut put, final InputStream is, final long length,
		final String contentType) {
		return put(put, is, length, contentType, null);
	}
	
	public HttpResponseEither<Exception,Future<HttpResponse>> put(
		final HttpPut put, final InputStream is, final long length,
		final String contentType, final HttpContext context) {
		if(is != null) {
			final InputStreamEntity entity = new InputStreamEntity(is, length);			
			if(contentType != null) {
				entity.setContentType(contentType);
			}
			put.setEntity(entity);
		}
		return request(put, context);
	}
	
	public HttpResponseEither<Exception,Future<HttpResponse>> delete(
		final String url) {
		return delete(create(url));
	}
	
	public HttpResponseEither<Exception,Future<HttpResponse>> delete(
		final URI uri) {
		return delete(new HttpDelete(uri));
	}
	
	public HttpResponseEither<Exception,Future<HttpResponse>> delete(
		final HttpDelete delete) {
		return delete(delete, null);
	}
	
	public HttpResponseEither<Exception,Future<HttpResponse>> delete(
		final HttpDelete delete, final HttpContext context) {
		return request(delete, context);
	}
	
	public HttpResponseEither<Exception,Future<HttpResponse>> request(
		final HttpRequestBase request) {
		return request(request, null);
	}
	
	public final HttpResponseEither<Exception,Future<HttpResponse>> request(
		final HttpRequestBase request, final HttpContext context) {
		return doit(request, (context == null) ?
			new BasicHttpContext() : context);
	}
	
	private final HttpResponseEither<Exception,Future<HttpResponse>> doit(
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
