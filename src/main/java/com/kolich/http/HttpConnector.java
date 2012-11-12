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

import static com.google.common.base.Preconditions.checkNotNull;
import static org.apache.http.client.protocol.ClientContext.COOKIE_STORE;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import com.kolich.http.exceptions.HttpClientClosureException;

public final class HttpConnector {
	
	/**
	 * The HttpClient that does all of the work.
	 */
	private final HttpClient httpClient_;
	
	public HttpConnector(HttpClient httpClient) {
		checkNotNull(httpClient, "HttpClient cannot be null.");
		httpClient_ = httpClient;
	}
	
	public HttpConnectorResponse doMethod(final HttpRequestBase method) {
		checkNotNull(method, "Incoming request method cannot be null.");
		// The context in which this execution operates.  This is
		// necessary so that if we hit a 30x Redirect, we can find out
		// where we landed (e.g., what was the target URL and host).
		final HttpContext context = new BasicHttpContext();
		// Setup a basic cookie store so that the response can fetch
		// any cookies returned by the server in the response.
		context.setAttribute(COOKIE_STORE, new BasicCookieStore());
		return doMethod(method, context);
	}
	
	public HttpConnectorResponse doMethod(final HttpRequestBase method,
		final HttpContext context) {
		checkNotNull(method, "Request method cannot be null.");
		checkNotNull(context, "Request context cannot be null.");
		int statusCode = -1;
		URL url = null;
		Header[] headers = null;
		HttpEntity entity = null;
		try {
			// Cache the request URL.
			url = method.getURI().toURL();
			// Execute the request.
			final HttpResponse response = httpClient_.execute(method, context);
			// Extract the response headers.
			headers = response.getAllHeaders();
			// Get the status line.
			final StatusLine statusLine;
			if((statusLine = response.getStatusLine()) != null) {
				statusCode = statusLine.getStatusCode();
			}
			entity = response.getEntity();
			return new HttpConnectorResponse(url, statusCode, headers,
				entity, context);
		} catch (Exception e) {
			method.abort();
			throw new HttpClientClosureException(new HttpConnectorResponse(url,
				statusCode, headers, entity, context), e);
		} finally {
			closeExpiredConnections();
		}
	}
	
	public HttpConnectorResponse doGet(final HttpGet get) {
		return doMethod(get);
	}
	
	/**
	 * Issues an Http GET to the requested URL.
	 * @param url The URL to GET.
	 * @return
	 */
	public HttpConnectorResponse doGet(final URL url) {
		checkNotNull(url, "URL cannot be null.");
		return doGet(new HttpGet(url.toString()));
	}
	
	/**
	 * Same as <code>doGet(URL url)</code> but accepts the
	 * URL argument as a String and does an implicit conversion
	 * to <code>java.net.URL(...)</code> before handing
	 * it off to the HttpClient.  This is to guarantee that the incoming
	 * URL is well formed.
	 * @param url
	 * @return
	 */
	public HttpConnectorResponse doGet(final String url) {
		checkNotNull(url, "URL cannot be null.");
		try {
			return doGet(new URL(url));
		} catch (MalformedURLException e) {
			throw new HttpClientClosureException("Malformed URL: " + url, e);
		}
	}
	
	public HttpConnectorResponse doPost(final HttpPost post) {
		return doMethod(post);
	}
		
	/**
	 * Issues an Http POST to the requested URL using the POST body and
	 * specified Content-Type.
	 * @param url
	 * @param data
	 * @param contentType
	 * @return
	 */
	public HttpConnectorResponse doPost(final URL url, final byte[] body,
		final String contentType) {
		checkNotNull(url, "URL cannot be null.");
		checkNotNull(body, "Request body cannot be null.");
		checkNotNull(contentType, "Content-Type cannot be null.");
		final HttpPost method = new HttpPost(url.toString());
		final ByteArrayEntity requestEntity = new ByteArrayEntity(body);
		requestEntity.setContentType(contentType);
		// Set the request entity.
		method.setEntity(requestEntity);
		return doPost(method);		
	}
	
	/**
	 * Same as <code>doPost(URL url, ...)</code> but accepts the
	 * URL argument as a String and does an implicit conversion
	 * to <code>java.net.URL(...)</code> before handing
	 * it off to the HttpClient.  This is to guarantee that the incoming
	 * URL is well formed.
	 * @param url
	 * @return
	 */
	public HttpConnectorResponse doPost(final String url, final byte[] body,
		final String contentType) {
		checkNotNull(url, "URL cannot be null.");
		checkNotNull(body, "Request body cannot be null.");
		checkNotNull(contentType, "Content-Type cannot be null.");
		try {
			return doPost(new URL(url), body, contentType);
		} catch (MalformedURLException e) {
			throw new HttpClientClosureException("Oops, malformed URL: " + url, e);
		}
	}
	
	public HttpConnectorResponse doPut(final HttpPut put) {
		return doMethod(put);
	}
	
	/**
	 * Issues an Http PUT to the requested URL using
	 * the PUT body and specified Content-Type.
	 * @param url
	 * @param data
	 * @param contentType
	 * @return
	 */
	public HttpConnectorResponse doPut(final URL url, final byte[] body,
		final String contentType) {
		checkNotNull(url, "URL cannot be null.");
		checkNotNull(body, "Request body cannot be null.");
		checkNotNull(contentType, "Content-Type cannot be null.");
		final HttpPut method = new HttpPut(url.toString());
		final ByteArrayEntity requestEntity = new ByteArrayEntity(body);
		requestEntity.setContentType(contentType);
		// Set the request entity.
		method.setEntity(requestEntity);
		return doPut(method);
	}
	
	/**
	 * Same as <code>doPut(URL url, ...)</code> but accepts the
	 * URL argument as a String and does an implicit conversion
	 * to <code>java.net.URL(...)</code> before handing
	 * it off to the HttpClient.  This is to guarantee that the incoming
	 * URL is well formed.
	 * @param url
	 * @return
	 */
	public HttpConnectorResponse doPut(final String url, final byte[] body,
		final String contentType) {
		checkNotNull(url, "URL cannot be null.");
		checkNotNull(body, "Request body cannot be null.");
		checkNotNull(contentType, "Content-Type cannot be null.");
		try {
			return doPut(new URL(url), body, contentType);
		} catch (MalformedURLException e) {
			throw new HttpClientClosureException("Oops, malformed URL: " + url, e);
		}
	}
	
	public HttpConnectorResponse doDelete(final HttpDelete delete) {
		return doMethod(delete);
	}
	
	/**
	 * Issues an Http DELETE to the requested URL.
	 * @param url The URL to DELETE.
	 * @return
	 */
	public HttpConnectorResponse doDelete(final URL url) {
		checkNotNull(url, "URL cannot be null.");
		return doDelete(new HttpDelete(url.toString()));
	}
	
	/**
	 * Same as <code>doDelete(URL url)</code> but accepts the
	 * URL argument as a String and does an implicit conversion
	 * to <code>java.net.URL(...)</code> before handing
	 * it off to the HttpClient.  This is to guarantee that the incoming
	 * URL is well formed.
	 * @param url
	 * @return
	 */
	public HttpConnectorResponse doDelete(final String url) {
		checkNotNull(url, "URL cannot be null.");
		try {
			return doDelete(new URL(url));
		} catch (MalformedURLException e) {
			throw new HttpClientClosureException("Oops, malformed URL: " + url, e);
		}
	}
	
	public void shutdown() {
		httpClient_.getConnectionManager().shutdown();
	}
	
	public void closeExpiredConnections() {
		httpClient_.getConnectionManager().closeExpiredConnections();
	}
		
}
