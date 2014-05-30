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

import com.kolich.http.HttpClient4Closure;
import com.kolich.http.common.response.HttpFailure;
import com.kolich.http.common.response.HttpSuccess;
import com.kolich.http.helpers.definitions.CustomEntityConverter;
import com.kolich.http.helpers.definitions.CustomFailureEntityConverter;
import com.kolich.http.helpers.definitions.CustomSuccessEntityConverter;
import org.apache.http.client.HttpClient;

import static com.kolich.http.HttpClient4ClosureBuilder.Factory.getNewInstanceWithProxySelector;

public final class EntityConverterClosures {
	
	// Cannot instantiate.
	private EntityConverterClosures() {}
	
	public static class CustomEntityConverterClosure<F,S>
		extends HttpClient4Closure<F,S> {
		private final CustomEntityConverter<F,S> converter_;
		public CustomEntityConverterClosure(final HttpClient client,
                                            final CustomEntityConverter<F,S> converter) {
			super(client);
			converter_ = converter;
		}
		public CustomEntityConverterClosure(final CustomEntityConverter<F,S> converter) {
			this(getNewInstanceWithProxySelector(), converter);
		}
		@Override
		public S success(final HttpSuccess success) throws Exception {
			return converter_.success(success);
		}
		@Override
		public F failure(final HttpFailure failure) {
			return converter_.failure(failure);
		}
	}
	
	public static class CustomEntitySeparateConverterClosure<F,S>
		extends CustomEntityConverterClosure<F,S> {
		public CustomEntitySeparateConverterClosure(final HttpClient client,
                                                    final CustomSuccessEntityConverter<S> success,
                                                    final CustomFailureEntityConverter<F> failure) {
			super(client, new CustomEntityConverter<F,S>() {
				@Override
				public S success(final HttpSuccess hSuccess) throws Exception {
					return success.success(hSuccess);
				}
				@Override
				public F failure(final HttpFailure hFailure) {
					return failure.failure(hFailure);
				}
			});
		}
		public CustomEntitySeparateConverterClosure(final CustomSuccessEntityConverter<S> success,
                                                    final CustomFailureEntityConverter<F> failure) {
			this(getNewInstanceWithProxySelector(), success, failure);
		}
	}

}
