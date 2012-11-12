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

package com.kolich.http.closure;

import static com.kolich.http.HttpConnectorResponse.consumeQuietly;
import static org.apache.http.HttpStatus.SC_BAD_REQUEST;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.InputStreamEntity;

import com.kolich.http.exceptions.HttpConnectorException;

public abstract class HttpClientClosure<F,S> {
				
	private final HttpClient client_;
	
	public HttpClientClosure(final HttpClient client) {
		client_ = client;
	}
	
	public HttpResponseEither<F,S> get(final String url) {
		try {
			return get(new URL(url));
		} catch (MalformedURLException e) {
			throw new HttpConnectorException(e);
		}
	}
	
	public HttpResponseEither<F,S> get(final URL url) {
		try {
			return get(new HttpGet(url.toURI()));
		} catch (URISyntaxException e) {
			throw new HttpConnectorException(e);
		}
	}
	
	public HttpResponseEither<F,S> get(final HttpGet get) {
		return doit(get);
	}
	
	public HttpResponseEither<F,S> post(final String url) {
		try {
			return post(new URL(url));
		} catch (MalformedURLException e) {
			throw new HttpConnectorException(e);
		}
	}
	
	public HttpResponseEither<F,S> post(final URL url) {
		try {
			return post(new HttpPost(url.toURI()), null, null);
		} catch (URISyntaxException e) {
			throw new HttpConnectorException(e);
		}
	}
	
	public HttpResponseEither<F,S> post(final HttpPost post, final byte[] body,
		final String contentType) {
		return post(post,
			(body != null) ? new ByteArrayInputStream(body) : null,
			(body != null) ? (long)body.length : -1L,
			contentType);
	}
	
	public HttpResponseEither<F,S> post(final HttpPost post,
		final InputStream is, final long length, final String contentType) {
		if(is != null) {
			final InputStreamEntity entity = new InputStreamEntity(is, length);			
			if(contentType != null) {
				entity.setContentType(contentType);
			}
			post.setEntity(entity);
		}
		return doit(post);
	}
	
	public HttpResponseEither<F,S> put(final String url) {
		try {
			return put(new URL(url));
		} catch (MalformedURLException e) {
			throw new HttpConnectorException(e);
		}
	}
	
	public HttpResponseEither<F,S> put(final URL url) {
		try {
			return put(new HttpPut(url.toURI()), null, null);
		} catch (URISyntaxException e) {
			throw new HttpConnectorException(e);
		}
	}
	
	public HttpResponseEither<F,S> put(final HttpPut put, final byte[] body) {
		return put(put, body, null);
	}
	
	public HttpResponseEither<F,S> put(final HttpPut put, final byte[] body,
		final String contentType) {
		return put(put,
			(body != null) ? new ByteArrayInputStream(body) : null,
			(body != null) ? (long)body.length : -1L,
			contentType);
	}
	
	public HttpResponseEither<F,S> put(final HttpPut put, final InputStream is,
		final long length, final String contentType) {
		if(is != null) {
			final InputStreamEntity entity = new InputStreamEntity(is, length);			
			if(contentType != null) {
				entity.setContentType(contentType);
			}
			put.setEntity(entity);
		}
		return doit(put);
	}
	
	public HttpResponseEither<F,S> delete(final String url) {
		try {
			return delete(new URL(url));
		} catch (MalformedURLException e) {
			throw new HttpConnectorException(e);
		}
	}
	
	public HttpResponseEither<F,S> delete(final URL url) {
		try {
			return delete(new HttpDelete(url.toURI()));
		} catch (URISyntaxException e) {
			throw new HttpConnectorException(e);
		}
	}
	
	public HttpResponseEither<F,S> delete(final HttpDelete get) {
		return doit(get);
	}
	
	public HttpResponseEither<F,S> request(final HttpRequestBase request) {
		return doit(request);
	}
	
	private final HttpResponseEither<F,S> doit(final HttpRequestBase request) {		
		final HttpResponseEither<HttpFailure,HttpSuccess> response = execute(request);
		try {
			if(response.success()) {
				return Right.right(success(((Right<HttpFailure,HttpSuccess>)
					response).right_));
			} else {
				return Left.left(failure(((Left<HttpFailure,HttpSuccess>)
					response).left_));
			}
		} catch (Exception e) {
			throw new HttpConnectorException(e);
		} finally {
			if(response.success()) {
				consumeQuietly(((Right<HttpFailure,HttpSuccess>)response)
					.right_.response_);
			} else {
				consumeQuietly(((Left<HttpFailure,HttpSuccess>)response)
					.left_.response_);
			}
		}
	}
	
	private final HttpResponseEither<HttpFailure,HttpSuccess> execute(
		final HttpRequestBase request) {
		HttpResponse response = null;
		int status = -1;
		try {
			before(request);
			response = client_.execute(request);
			status = response.getStatusLine().getStatusCode(); 
			if(status < SC_BAD_REQUEST) {
				return Right.right(new HttpSuccess(response, status));
			} else {
				return Left.left(new HttpFailure(null, response, status));
			}
		} catch (ClientProtocolException e) {
			request.abort();
			return Left.left(new HttpFailure(e, response, status));
		} catch (IOException e) {
			request.abort();
			return Left.left(new HttpFailure(e, response, status));
		} catch (Exception e) {
			request.abort();
			return Left.left(new HttpFailure(e, response, status));
		}
	}

	public void before(final HttpRequestBase request) throws Exception {
		// Default, do nothing.
	}	
	public abstract S success(final HttpSuccess success) throws Exception;
	public F failure(final HttpFailure failure) throws Exception {
		return null; // Default, return null.
	}
	
	public static abstract class HttpClientClosureResponse {
		public final HttpResponse response_;
		public final int status_;
		public HttpClientClosureResponse(HttpResponse response, int status) {
			response_ = response;
			status_ = status;
		}
	}
	public static final class HttpFailure extends HttpClientClosureResponse {		
		public final Exception cause_;
		public HttpFailure(Exception cause, HttpResponse response, int status) {
			super(response, status);
			cause_ = cause;
		}
		public HttpFailure(HttpResponse response) {
			this(null, response, -1);
		}
	}
	public static final class HttpSuccess extends HttpClientClosureResponse {
		public HttpSuccess(HttpResponse response, int status) {
			super(response,status);
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
