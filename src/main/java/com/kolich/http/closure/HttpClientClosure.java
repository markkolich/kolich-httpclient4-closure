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

public abstract class HttpClientClosure<T> {
				
	private final HttpClient client_;
	
	public HttpClientClosure(final HttpClient client) {
		client_ = client;
	}
	
	public T get(final String url) {
		try {
			return get(new URL(url));
		} catch (MalformedURLException e) {
			throw new HttpConnectorException(e);
		}
	}
	
	public T get(final URL url) {
		try {
			return get(new HttpGet(url.toURI()));
		} catch (URISyntaxException e) {
			throw new HttpConnectorException(e);
		}
	}
	
	public T get(final HttpGet get) {
		return doit(get);
	}
	
	public T post(final String url) {
		try {
			return post(new URL(url));
		} catch (MalformedURLException e) {
			throw new HttpConnectorException(e);
		}
	}
	
	public T post(final URL url) {
		try {
			return post(new HttpPost(url.toURI()), null, null);
		} catch (URISyntaxException e) {
			throw new HttpConnectorException(e);
		}
	}
	
	public T post(final HttpPost post, final byte[] body,
		final String contentType) {
		return post(post,
			(body != null) ?
				new ByteArrayInputStream(body) :
				null,
			(body != null) ?
				(long)body.length :
				-1L,
			contentType);
	}
	
	public T post(final HttpPost post, final InputStream is, final long length,
		final String contentType) {
		if(is != null) {
			final InputStreamEntity entity = new InputStreamEntity(is, length);			
			if(contentType != null) {
				entity.setContentType(contentType);
			}
			post.setEntity(entity);
		}
		return doit(post);
	}
	
	public T put(final String url) {
		try {
			return put(new URL(url));
		} catch (MalformedURLException e) {
			throw new HttpConnectorException(e);
		}
	}
	
	public T put(final URL url) {
		try {
			return put(new HttpPut(url.toURI()), null, null);
		} catch (URISyntaxException e) {
			throw new HttpConnectorException(e);
		}
	}
	
	public T put(final HttpPut put, final byte[] body,
		final String contentType) {
		return put(put,
			(body != null) ?
				new ByteArrayInputStream(body) :
				null,
			(body != null) ?
				(long)body.length :
				-1L,
			contentType);
	}
	
	public T put(final HttpPut put, final InputStream is, final long length,
		final String contentType) {
		if(is != null) {
			final InputStreamEntity entity = new InputStreamEntity(is, length);			
			if(contentType != null) {
				entity.setContentType(contentType);
			}
			put.setEntity(entity);
		}
		return doit(put);
	}
	
	public T delete(final String url) {
		try {
			return delete(new URL(url));
		} catch (MalformedURLException e) {
			throw new HttpConnectorException(e);
		}
	}
	
	public T delete(final URL url) {
		try {
			return delete(new HttpDelete(url.toURI()));
		} catch (URISyntaxException e) {
			throw new HttpConnectorException(e);
		}
	}
	
	public T delete(final HttpDelete get) {
		return doit(get);
	}
	
	private final T doit(final HttpRequestBase request) {		
		final HttpResponseEither<HttpFailure,HttpSuccess> resp = execute(request);
		try {
			return (resp.success()) ?
				success(((Right<HttpFailure,HttpSuccess>)resp).right_) :
				failure(((Left<HttpFailure,HttpSuccess>)resp).left_);
		} catch (Exception e) {
			throw new HttpConnectorException(e);
		} finally {
			if(resp.success()) {
				consumeQuietly(((Right<HttpFailure,HttpSuccess>)resp).right_.response_);
			} else {
				consumeQuietly(((Left<HttpFailure,HttpSuccess>)resp).left_.response_);
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
	
	public abstract T success(final HttpSuccess success) throws Exception;	
	public T failure(final HttpFailure failure) throws Exception {
		return null; // Default, return null.
	}
	
	public static final class HttpFailure {
		public final HttpResponse response_;
		public final Exception cause_;
		public final int status_;
		public HttpFailure(Exception cause, HttpResponse response, int status) {
			cause_ = cause;
			response_ = response;
			status_ = status;
		}
		public HttpFailure(HttpResponse response) {
			this(null, response, -1);
		}
	}
	public static final class HttpSuccess {
		public final HttpResponse response_;
		public final int status_;
		public HttpSuccess(HttpResponse response, int status) {
			response_ = response;
			status_ = status;
		}
	}
	
	private static abstract class HttpResponseEither<A,B> {
		public abstract boolean success();
	}
			
	private static final class Left<A,B> extends HttpResponseEither<A,B> {
		public final A left_;
		private Left(final A left) {
			left_ = left;
		}
		@Override
		public boolean success() {
			return false;
		}
		public static final <A,B> HttpResponseEither<A,B> left(final A left) {
			return new Left<A,B>(left);
		}
	}

	private static final class Right<A,B> extends HttpResponseEither<A,B> {
		public final B right_;
		private Right(final B right) {
			right_ = right;
		}
		@Override
		public boolean success() {
			return true;
		}
		public static final <A,B> HttpResponseEither<A,B> right(final B right) {
			return new Right<A,B>(right);
		}
	}
	
}
