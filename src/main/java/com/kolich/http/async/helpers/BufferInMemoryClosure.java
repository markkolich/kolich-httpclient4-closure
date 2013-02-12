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

package com.kolich.http.async.helpers;

import java.io.IOException;

import org.apache.http.ContentTooLongException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.ContentDecoder;
import org.apache.http.nio.IOControl;
import org.apache.http.nio.client.HttpAsyncClient;
import org.apache.http.nio.entity.ContentBufferEntity;
import org.apache.http.nio.protocol.AbstractAsyncResponseConsumer;
import org.apache.http.nio.protocol.HttpAsyncResponseConsumer;
import org.apache.http.nio.util.HeapByteBufferAllocator;
import org.apache.http.nio.util.SimpleInputBuffer;
import org.apache.http.protocol.HttpContext;

import com.kolich.http.async.HttpAsyncClient4Closure;
import com.kolich.http.common.either.HttpResponseEither;
import com.kolich.http.common.either.Left;
import com.kolich.http.common.either.Right;
import com.kolich.http.common.response.HttpFailure;
import com.kolich.http.common.response.HttpSuccess;

public abstract class BufferInMemoryClosure<F,S>
	extends HttpAsyncClient4Closure<F,S> {

	public BufferInMemoryClosure(final HttpAsyncClient client) {
		super(client);
	}
	
	@Override
	public final HttpAsyncResponseConsumer<HttpResponseEither<F,S>> getConsumer() {
		return new AbstractAsyncResponseConsumer<HttpResponseEither<F,S>>() {
			private volatile HttpResponse response_;
		    private volatile SimpleInputBuffer buf_;
			@Override
			protected void onResponseReceived(final HttpResponse response)
				throws HttpException, IOException {
				response_ = response;
			}
			@Override
			protected void onContentReceived(final ContentDecoder decoder,
				final IOControl ioctrl) throws IOException {
				if(buf_ == null) {
		            throw new IllegalStateException("Content buffer is null?");
		        }
		        buf_.consumeContent(decoder);
			}
			@Override
			protected void onEntityEnclosed(final HttpEntity entity,
				final ContentType contentType) throws IOException {
				long len = entity.getContentLength();
		        if (len > Integer.MAX_VALUE) {
		            throw new ContentTooLongException("Entity content is too long: " + len);
		        } else if (len < 0) {
		            len = 4096;
		        }
		        buf_ = new SimpleInputBuffer((int) len, new HeapByteBufferAllocator());
		        response_.setEntity(new ContentBufferEntity(entity, buf_));
			}
			@Override
			protected HttpResponseEither<F,S> buildResult(final HttpContext context)
				throws Exception {
				HttpResponseEither<F,S> result = null;
				try {						
					// Check if the response was "successful".  The
					// definition of success is arbitrary based on
					// what's defined in the check() method.  The
					// default success check is simply checking the
					// HTTP status code and if it's less than 400
					// (Bad Request) then it's considered "good".
					// If the user wants evaluate this response
					// against some custom criteria, they should
					// override this check() method.
					if(check(response_, context)) {
						result = Right.right(success(new HttpSuccess(response_, context)));
					} else {
						result = Left.left(failure(new HttpFailure(response_, context)));
					}
				} catch (Exception e) {
					result = Left.left(failure(new HttpFailure(e)));
				}
				return result;
			}
			@Override
			protected void releaseResources() {
				buf_ = null;
				response_ = null;
			}
		};
	}

}
