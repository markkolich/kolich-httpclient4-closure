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

package com.kolich.http.helpers;

import com.kolich.http.common.response.HttpSuccess;
import com.kolich.http.helpers.definitions.IgnoreResultClosure;
import com.kolich.http.helpers.definitions.OrHttpFailureClosure;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.protocol.HttpContext;

import java.util.Arrays;
import java.util.List;

public final class StatusCodeAndHeaderClosures {
	
	// Cannot instantiate.
	private StatusCodeAndHeaderClosures() {}
	
	/**
	 * Extracts the list of HTTP response headers from the response, regardless
	 * if the request completed successfully or not.
	 */
	public static class HeadersOnlyClosure extends IgnoreResultClosure {		
		private Header[] headers_ = null;		
		public HeadersOnlyClosure(final HttpClient client) {
			super(client);
		}
		public HeadersOnlyClosure() {
			super();
		}
		@Override
		public void after(final HttpResponse response,
			final HttpContext context) {
			headers_ = response.getAllHeaders();
		}
		public final Header[] getHeaders() {
			return headers_;
		}
		public final List<Header> getHeaderList() {
			return (headers_ == null) ? null : Arrays.asList(headers_);
		}
	}
	
	/**
	 * Extracts the HTTP status code and list of response headers from the
	 * response, regardless if the request completed successfully or not.
	 */
	public static class StatusCodeAndHeadersClosure extends HeadersOnlyClosure {
		private int statusCode_ = -1;
		public StatusCodeAndHeadersClosure(final HttpClient client) {
			super(client);
		}
		public StatusCodeAndHeadersClosure() {
			super();
		}
		@Override
		public void after(final HttpResponse response,
			final HttpContext context) {
			statusCode_ = response.getStatusLine().getStatusCode();
			super.after(response, context);
		}
		public final int getStatusCode() {
			return statusCode_;
		}
	}
	
	public static class StatusCodeOrHttpFailureClosure
		extends OrHttpFailureClosure<Integer> {
		public StatusCodeOrHttpFailureClosure(final HttpClient client) {
			super(client);
		}
		public StatusCodeOrHttpFailureClosure() {
			super();
		}
		@Override
		public final Integer success(final HttpSuccess success)
			throws Exception {
			return success.getStatusCode();
		}
	}

}
