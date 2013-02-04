package com.kolich.http.response;

import org.apache.http.HttpResponse;
import org.apache.http.protocol.HttpContext;

public final class HttpSuccess extends HttpClientClosureResponse {
	
	public HttpSuccess(HttpResponse response, HttpContext context) {
		super(response, context);
	}
	
}