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

package com.kolich.http.common;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.apache.http.client.methods.HttpRequestBase;

import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static java.lang.System.currentTimeMillis;
import static java.util.concurrent.Executors.newSingleThreadExecutor;

/* package private */
abstract class ClosureRequestTimeoutable {

    /**
     * The internal default request timeout is zero, meaning never timeout.
     */
    protected static final long DEFAULT_REQUEST_TIMEOUT_MS = 0L;

    private static final String HTTPCLIENT4_CLOSURE_TIMEOUT_MGR_THREAD_NAME =
        "kolich-httpclient4-closure-timeout-manager-%d";

    protected static final DelayQueue<ClosureDelayable<HttpRequestBase>> timeoutQueue__;
    static {
        timeoutQueue__ = new DelayQueue<>();
        newSingleThreadExecutor(new ThreadFactoryBuilder()
            .setDaemon(true)
            .setNameFormat(HTTPCLIENT4_CLOSURE_TIMEOUT_MGR_THREAD_NAME)
            .build())
            .submit(() -> {
                while (true) {
                    try {
                        // Retrieves and removes the head of this queue,
                        // waiting if necessary until an element with an
                        // expired delay is available on this queue.
                        final ClosureDelayable delayable = timeoutQueue__.take();
                        // Abort the request attached to the delayable.
                        delayable.request_.abort();
                    } catch (Exception e) { }
                }
            });
    }

    protected static final class ClosureDelayable<T extends HttpRequestBase>
        implements Delayed {
        private final T request_;
        private final long expiresAt_;
        public ClosureDelayable(final T request,
                                final long expiresAt) {
            request_ = checkNotNull(request, "Request cannot be null.");
            checkState(expiresAt > currentTimeMillis(), "Expiry time cannot be in the past.");
            expiresAt_ = expiresAt;
        }
        @Override
        public final long getDelay(final TimeUnit unit) {
            return unit.convert(expiresAt_ - currentTimeMillis(), TimeUnit.MILLISECONDS);
        }
        @Override
        public final int compareTo(final Delayed d) {
            checkNotNull(d, "Comparison delayable cannot be null.");
            return Long.compare(expiresAt_, ((ClosureDelayable)d).expiresAt_);
        }
    }

    /**
     * The request timeout is defined here as the time it takes for the
     * request to be sent until a ~complete~ response is received.  That is,
     * even if a request is sent and response bytes are trickling in from the
     * server if this client hasn't received a "full" response when this
     * timeout is hit, then the entire request is aborted and all streams
     * are forcibly closed.
     *
     * A timeout value of zero is interpreted as an infinite timeout
     * (e.g., never timeout).
     */
    protected long requestTimeoutMs_ = DEFAULT_REQUEST_TIMEOUT_MS;

}
