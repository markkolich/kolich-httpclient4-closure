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

package com.kolich.http.blocking.helpers;

import static com.kolich.common.DefaultCharacterEncoding.UTF_8;
import static com.kolich.http.blocking.KolichDefaultHttpClient.KolichHttpClientFactory.getNewInstanceWithProxySelector;

import org.apache.http.client.HttpClient;
import org.apache.http.util.EntityUtils;

import com.kolich.http.blocking.helpers.definitions.OrHttpFailureClosure;
import com.kolich.http.blocking.helpers.definitions.OrNullClosure;
import com.kolich.http.common.response.HttpSuccess;

public final class StringClosures {
	
	// Cannot instantiate.
	private StringClosures() {}
	
	public static class StringOrHttpFailureClosure extends OrHttpFailureClosure<String> {	
		private final String defaultCharset_;		
		public StringOrHttpFailureClosure(final HttpClient client,
			final String defaultCharset) {
			super(client);
			defaultCharset_ = defaultCharset;
		}
		public StringOrHttpFailureClosure(final HttpClient client) {
			this(client, UTF_8);
		}
		public StringOrHttpFailureClosure() {
			this(getNewInstanceWithProxySelector());
		}
		@Override
		public final String success(final HttpSuccess success)
			throws Exception {
			return EntityUtils.toString(
				success.getResponse().getEntity(),
				defaultCharset_);
		}
	}
	
	public static class StringOrNullClosure extends OrNullClosure<String> {		
		private final String defaultCharset_;		
		public StringOrNullClosure(final HttpClient client,
			final String defaultCharset) {
			super(client);
			defaultCharset_ = defaultCharset;
		}
		public StringOrNullClosure(final HttpClient client) {
			this(client, UTF_8);
		}
		public StringOrNullClosure() {
			this(getNewInstanceWithProxySelector());
		}
		@Override
		public final String success(final HttpSuccess success)
			throws Exception {
			return EntityUtils.toString(
				success.getResponse().getEntity(),
				defaultCharset_);
		}
	}

}
