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

package com.kolich.http.blocking.helpers.definitions;

import static com.kolich.http.blocking.KolichDefaultHttpClient.KolichHttpClientFactory.getNewInstanceNoProxySelector;

import org.apache.http.client.HttpClient;

import com.kolich.http.blocking.HttpClient4Closure;
import com.kolich.http.common.response.HttpFailure;
import com.kolich.http.common.response.HttpSuccess;

/**
 * This abstract closure is used when you don't care whether
 * the request completed successfully or not, just that it completed.
 */
public abstract class IgnoreResultClosure extends HttpClient4Closure<Void,Void> {

	public IgnoreResultClosure(final HttpClient client) {
		super(client);
	}
	
	public IgnoreResultClosure() {
		this(getNewInstanceNoProxySelector());
	}
	
	@Override
	public final Void success(final HttpSuccess success) {
		return null;
	}
	
	@Override
	public final Void failure(final HttpFailure failure) {
		return null;
	}
	
}
