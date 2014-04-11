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

package com.kolich.http.helpers;

import com.kolich.http.common.response.HttpSuccess;
import com.kolich.http.helpers.definitions.OrHttpFailureClosure;
import com.kolich.http.helpers.definitions.OrNullClosure;
import org.apache.http.client.HttpClient;
import org.apache.http.util.EntityUtils;

public final class ByteArrayClosures {
	
	// Cannot instantiate.
	private ByteArrayClosures() {}
	
	public static class ByteArrayOrHttpFailureClosure extends OrHttpFailureClosure<byte[]> {		
		public ByteArrayOrHttpFailureClosure(final HttpClient client) {
			super(client);
		}
		public ByteArrayOrHttpFailureClosure() {
			super();
		}
		@Override
		public final byte[] success(final HttpSuccess success) throws Exception {
			return EntityUtils.toByteArray(success.getResponse().getEntity());
		}
	}
	
	public static class ByteArrayOrNullClosure extends OrNullClosure<byte[]> {		
		public ByteArrayOrNullClosure(final HttpClient client) {
			super(client);
		}
		public ByteArrayOrNullClosure() {
			super();
		}
		@Override
		public final byte[] success(final HttpSuccess success) throws Exception {
			return EntityUtils.toByteArray(success.getResponse().getEntity());
		}
	}
	
}


