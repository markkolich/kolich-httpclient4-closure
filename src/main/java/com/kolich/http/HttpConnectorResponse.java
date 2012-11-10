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

import static org.apache.http.HttpHeaders.CONTENT_LANGUAGE;
import static org.apache.http.HttpHeaders.CONTENT_LENGTH;
import static org.apache.http.HttpHeaders.CONTENT_TYPE;
import static org.apache.http.HttpHeaders.ETAG;
import static org.apache.http.HttpHeaders.LAST_MODIFIED;
import static org.apache.http.HttpStatus.SC_NO_CONTENT;
import static org.apache.http.HttpStatus.SC_OK;
import static org.apache.http.client.protocol.ClientContext.COOKIE_STORE;
import static org.apache.http.util.EntityUtils.consume;

import java.net.URL;
import java.util.Date;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.protocol.HttpContext;

import com.kolich.common.date.RFC822DateFormat;

public final class HttpConnectorResponse {
		
	/**
	 * The URL of this request.
	 */
	private final URL url_;
	
	/**
	 * The HTTP status code of the response.
	 */
	private final int statusCode_;
	
	/**
	 * Any HTTP response headers.
	 */
	private final Header[] headers_;
	
	/**
	 * The HTTP response body.
	 */
	private final HttpEntity entity_;
	
	/**
	 * The HTTP response context; includes cookies and other
	 * meta data in the response that we might care about later.
	 */
	private final HttpContext context_;
	
	public HttpConnectorResponse(URL url, int status, Header[] headers,
		HttpEntity entity, HttpContext context) {
		url_ = url;
		statusCode_ = status;
		headers_ = headers;
		entity_ = entity;
		context_ = context;
	}
		
	/**
	 * Returns the Http status code of this response as set
	 * by the Apache Commons HttpClient.
	 * @return
	 */
	public int getStatus() {
		return statusCode_;
	}
	
	/**
	 * Returns true if this response was a 2xx response.
	 * @return
	 */
	public boolean ok2xx() {
		return (statusCode_ / 100 == SC_OK / 100);
	}
	
	/**
	 * Returns true if this response was an exact 200 OK response.
	 * Any other status code, even other codes in the 2xx range, will
	 * return false.
	 * @return
	 */
	public boolean ok() {
		return (statusCode_ == SC_OK);
	}
	
	/**
	 * Returns true if this response was an exact 204 No Content response.
	 * Any other status code, even other codes in the 2xx range, will
	 * return false.
	 * @return
	 */
	public boolean okNoContent() {
		return (statusCode_ == SC_NO_CONTENT);
	}
	
	/**
	 * Returns the URL attached to this request.
	 * @return
	 */
	public URL getUrl() {
		return url_;
	}
	
	/**
	 * Returns all of the Http headers in this response.
	 * @return
	 */
	public Header[] getHeaders() {
		return headers_;
	}
	
	/**
	 * Returns the HTTP {@link Header} in this response with the given
	 * name.  If no header with the given name exists, returns
	 * null.  If multiple headers are found with the same name
	 * the first one found is returned.
	 * @param headerName
	 * @return
	 */
	public Header getHeader(final String name) {
		Header header = null;
		if(headers_ != null) {
			for(final Header check : headers_) {
				if(check.getName().equalsIgnoreCase(name)) {
					header = check;
					break;
				}
			}
		}		
		return header;
	}
	
	/**
	 * Returns the HTTP {@link Header} value in this response with
	 * the given name.  If no header with the given name exists, returns
	 * null.  If multiple headers are found with the same name
	 * the first one found is returned.
	 * @param headerName
	 * @return
	 */
	public String getHeaderValue(final String name) {
		final Header header;
		if((header = getHeader(name)) != null) {
			return header.getValue();
		} else {
			return null;
		}
	}
	
	/**
	 * Returns the content of the "Content-Type" HTTP response
	 * header, if it exists.  May be null on responses that do
	 * not contain a Content-Type header.
	 * @return
	 */
	public String getContentType() {
		final Header contentType;
		if((contentType = getHeader(CONTENT_TYPE)) != null) {
			return contentType.getValue();
		}
		return null;
	}
	
	/**
	 * Returns the content of the "Content-Language" HTTP response
	 * header, if it exists.  May be null on responses that do not
	 * contain a Content-Language header.
	 * @return
	 */
	public String getContentLanguage() {
		final Header contentLang;
		if((contentLang = getHeader(CONTENT_LANGUAGE)) != null) {
			return contentLang.getValue();
		}
		return null;
	}
	
	/**
	 * Returns the ETag on the response, if any.  If no ETag was
	 * present, returns null.
	 * @return
	 */
	public String getETag() {
		final Header eTag;
		if((eTag = getHeader(ETAG)) != null) {
			return eTag.getValue();
		}
		return null;
	}

	/**
	 * Returns the Last-Modified date, if any, attached to the response.
	 * If no Last-Modified header was found, returns null.
	 * @return
	 */
	public Date getLastModified() {
		final Header last;
		if((last = getHeader(LAST_MODIFIED)) != null) {
			return RFC822DateFormat.format(last.getValue());
		}
		return null;
	}
	 
	/**
	 * Returns the Content-Length header, if any, attached to the response.
	 * If no Content-Length header was found, returns -1L.
	 * @return
	 */
	public long getContentLength() {
		final Header length;
		if((length = getHeader(CONTENT_LENGTH)) != null) {
			return Long.parseLong(length.getValue());
		}
		return -1L;
	}
	
	/**
	 * Retrieves the {@link HttpEntity} associated with this request if one
	 * exists. May be null in the case that an entity was not, or could not,
	 * be extracted from the response.
	 * @return
	 */
	public HttpEntity getEntity() {
		return entity_;
	}
	
	/**
	 * Retrieves the HttpClient execution context of this
	 * response.  May be <code>null</code> if no valid execution
	 * context was tied to this request.
	 * @return
	 */
	public HttpContext getContext() {
		return context_;
	}
	
	/**
	 * Retrives the {@link CookieStore} contained in this context, if any.
	 * Returns null if no cookie store was found in this response context
	 * or if the context itself was null.
	 * @return
	 */
	public CookieStore getCookieStore() {
		CookieStore store = null;
		final HttpContext context;
		if((context = getContext()) != null) {
			// If we have a context, then see if we have any cookies lying
			// around under the context.  May return null if no cookie store
			// was found in this context.
			store = (CookieStore)context.getAttribute(COOKIE_STORE);
		}
		return store;
	}
	
	public void close() {
		consumeQuietly(getEntity());
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
		
	/**
	 * Quietly closes any {@link HttpEntity} in the provided
	 * {@link HttpConnectorResponse}, suppressing any exceptions. Ensures that
	 * the entity content is fully consumed and the content stream, if exists,
	 * is closed.
	 */
	public static final void consumeQuietly(final HttpConnectorResponse response) {
		if(response != null) {
			consumeQuietly(response.getEntity());
		}
	}
	
}
