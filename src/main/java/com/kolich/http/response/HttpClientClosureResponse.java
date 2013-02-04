package com.kolich.http.response;

import static org.apache.http.HttpHeaders.CONTENT_LENGTH;
import static org.apache.http.HttpHeaders.CONTENT_TYPE;
import static org.apache.http.HttpHeaders.ETAG;

import java.io.IOException;
import java.io.InputStream;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.protocol.HttpContext;

public abstract class HttpClientClosureResponse {
	
	private final HttpResponse response_;
	private final HttpContext context_;
	
	public HttpClientClosureResponse(HttpResponse response,
		HttpContext context) {
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
	
	public final String getContentType() {
		return getFirstHeader(CONTENT_TYPE);
	}
	
	public final String getContentLength() {
		return getFirstHeader(CONTENT_LENGTH);
	}
	
	public final String getETag() {
		return getFirstHeader(ETAG);
	}
	
}
