/**
 * Copyright (c) 2014 Mark S. Kolich
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

package com.kolich.http.common.response;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.protocol.HttpContext;

import java.io.IOException;
import java.io.InputStream;

import static org.apache.http.HttpHeaders.ETAG;

public abstract class HttpClientClosureResponse {
	
	private final HttpResponse response_;
	private final HttpContext context_;
	
	public HttpClientClosureResponse(final HttpResponse response,
                                     final HttpContext context) {
		response_ = response;
		context_ = context;
	}
	
	public final HttpResponse getResponse() {
		return response_;
	}
	
	public final HttpContext getContext() {
		return context_;
	}
	
	public final HttpEntity getEntity() {
		return (response_ != null) ? response_.getEntity() : null;
	}
	
	public final InputStream getContent() throws IllegalStateException, IOException {
		final HttpEntity entity;
		if((entity = getEntity()) != null) {
			return entity.getContent();
		}
		return null;
	}
	
	public final StatusLine getStatusLine() {
		return (response_ != null) ? response_.getStatusLine() : null;
	}
	
	public final int getStatusCode() {
		final StatusLine line;
		if((line = getStatusLine()) != null) {
			return line.getStatusCode();
		}
		return -1; // Unset
	}
	
	public final String getFirstHeader(final String headerName) {
		final Header header;
		if(response_ != null &&
			(header = response_.getFirstHeader(headerName)) != null) {
			return header.getValue();
		}
		return null;
	}
	
	public final String getETag() {
		return getFirstHeader(ETAG);
	}
	
	public final String getContentType() {
		final HttpEntity entity;
		if((entity = getEntity()) != null) {
			final Header contentType;
			if((contentType = entity.getContentType()) != null) {
				return contentType.getValue();
			}
		}
		return null;
	}
	
	public final long getContentLength() {
		final HttpEntity entity;
		if((entity = getEntity()) != null) {
			return entity.getContentLength();
		}
		return -1L;
	}
	
}
