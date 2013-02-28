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

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.ContentDecoder;
import org.apache.http.nio.IOControl;
import org.apache.http.nio.client.HttpAsyncClient;
import org.apache.http.nio.protocol.BasicAsyncResponseConsumer;
import org.apache.http.protocol.HttpContext;

import com.kolich.http.common.either.HttpResponseEither;
import com.kolich.http.common.either.Left;
import com.kolich.http.common.either.Right;
import com.kolich.http.common.response.HttpFailure;
import com.kolich.http.common.response.HttpSuccess;

public abstract class BufferInMemoryClosure<S>
	extends OrHttpFailureAsyncClosure<S> {
	
	private WrappedBasicAsyncResponseConsumer consumer_;
	
	public BufferInMemoryClosure(final HttpAsyncClient client) {
		super(client);
		consumer_ = new WrappedBasicAsyncResponseConsumer();
	}
	
	@Override
	public final void onResponseReceived(final HttpResponse response)
		throws IOException {
		consumer_.onResponseReceived(response);
	}
	
	@Override
	public final void onContentReceived(final ContentDecoder decoder,
		final IOControl ioctrl) throws IOException {
		consumer_.onContentReceived(decoder, ioctrl);
	}
	
	@Override
	public final void onEntityEnclosed(final HttpEntity entity,
		final ContentType contentType) throws IOException {
		consumer_.onEntityEnclosed(entity, contentType);
	}
	
	@Override
	public final HttpResponseEither<HttpFailure,S> buildResult(final HttpContext context)
		throws Exception {
		HttpResponseEither<HttpFailure,S> result = null;
		try {
			final HttpResponse response = consumer_.buildResult(context);
			// Check if the response was "successful".  The definition of
			// success is arbitrary based on what's defined in the check()
			// method.  The default success check is simply checking the
			// HTTP status code and if it's less than 400 (Bad Request) then
			// it's considered "good". If the user wants evaluate this response
			// against some custom criteria, they should override this
			// check() method.
			if(check(response, context)) {
				result = Right.right(success(new HttpSuccess(response, context)));
			} else {
				result = Left.left(failure(new HttpFailure(response, context)));
			}
		} catch (Exception e) {
			result = Left.left(failure(new HttpFailure(e)));
		}
		return result;
	}
	
	@Override
	public final void releaseResources() {		
		consumer_.releaseResources();		
	}
	
	private static class WrappedBasicAsyncResponseConsumer
		extends BasicAsyncResponseConsumer {
		
		public WrappedBasicAsyncResponseConsumer() {
			super();
		}
		
		@Override
		public final void onResponseReceived(final HttpResponse response)
			throws IOException {
			super.onResponseReceived(response);
		}
		
		@Override
		public final void onContentReceived(final ContentDecoder decoder,
			final IOControl ioctrl) throws IOException {
			super.onContentReceived(decoder, ioctrl);
		}
		
		@Override
		public final void onEntityEnclosed(final HttpEntity entity,
			final ContentType contentType) throws IOException {
			super.onEntityEnclosed(entity, contentType);
		}
		
		@Override
		public final HttpResponse buildResult(final HttpContext context) {
			return super.buildResult(context);
		}
		
		@Override
		public final void releaseResources() {
			super.releaseResources();
		}
		
	}
	
}
