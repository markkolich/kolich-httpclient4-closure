/**
 * Copyright (c) 2015 Mark S. Kolich
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

package com.kolich.http.common.response;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;

import java.io.Closeable;

import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.apache.http.util.EntityUtils.consumeQuietly;

public final class ResponseUtils {
	
	// Cannot instantiate
	private ResponseUtils() { }
	
	/**
	 * Quietly closes any {@link HttpEntity} in the provided
	 * {@link HttpResponse}, suppressing any exceptions. Ensures that
	 * the entity content is fully consumed and the content stream, if exists,
	 * is closed.  Also attempts to close the underlying {@link HttpResponse}
     * entity as well, if supported.
	 */
	public static final void consumeResponseQuietly(final HttpResponse response) {
		if(response != null) {
            // Consume the response entity, closing any associated streams.
            consumeQuietly(response.getEntity());
            // If the response itself is an instance of Closable, cannot hurt
            // to also close the higher order response too in the event that
            // request pools are waiting for a free connection.
            if(response instanceof Closeable) {
                closeQuietly(((Closeable)response));
            }
        }
	}

}
