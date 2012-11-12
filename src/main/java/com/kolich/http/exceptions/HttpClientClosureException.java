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

package com.kolich.http.exceptions;

import com.kolich.common.KolichCommonException;
import com.kolich.http.HttpConnectorResponse;

public final class HttpClientClosureException extends KolichCommonException {

	private static final long serialVersionUID = -8194826635345323997L;
	
	private HttpConnectorResponse response_;
	
	public HttpClientClosureException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public HttpClientClosureException(Throwable cause) {
		super(cause);
	}
	
	public HttpClientClosureException(String message) {
		super(message);
	}
	
	public HttpClientClosureException(HttpConnectorResponse response, Throwable cause) {
		this(cause);
		response_ = response;
	}
	
	public HttpClientClosureException(HttpConnectorResponse response, String message) {
		this(message);
		response_ = response;
	}
	
	/**
	 * Returns the {@link HttpConnectorResponse} associated with this
	 * exception case, if any. May be null.
	 * @return
	 */
	public HttpConnectorResponse getResponse() {
		return response_;
	}
	
}
