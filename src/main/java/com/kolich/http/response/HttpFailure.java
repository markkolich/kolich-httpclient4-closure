package com.kolich.http.response;

import org.apache.http.HttpResponse;
import org.apache.http.protocol.HttpContext;

public final class HttpFailure extends HttpClientClosureResponse {
	
	private final Exception cause_;
	
	public HttpFailure(Exception cause, HttpResponse response,
		HttpContext context) {
		super(response, context);
		cause_ = cause;
	}
	
	public HttpFailure(HttpResponse response, HttpContext context) {
		this(null, response, context);
	}
	
	public HttpFailure(Exception cause) {
		this(cause, null, null);
	}
	
	public Exception getCause() {
		return cause_;
	}
	
}
