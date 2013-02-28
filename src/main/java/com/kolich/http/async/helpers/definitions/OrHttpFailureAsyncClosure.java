/**
 * Copyright (c) 2013 Mark S. Kolich
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

package com.kolich.http.async.helpers.definitions;

import static com.kolich.http.async.KolichDefaultHttpAsyncClient.KolichHttpAsyncClientFactory.getNewAsyncInstanceWithProxySelector;

import org.apache.http.nio.client.HttpAsyncClient;

import com.kolich.http.async.HttpAsyncClient4Closure;
import com.kolich.http.common.response.HttpFailure;

/**
 * Abstract closure to return a proper {@link HttpFailure} on failure.
 */
public abstract class OrHttpFailureAsyncClosure<S>
	extends HttpAsyncClient4Closure<HttpFailure,S> {

	public OrHttpFailureAsyncClosure(final HttpAsyncClient client) {
		super(client);
	}
	
	public OrHttpFailureAsyncClosure() {
		this(getNewAsyncInstanceWithProxySelector());
	}
	
	@Override
	public final HttpFailure failure(final HttpFailure failure) {
		return failure;
	}

}
