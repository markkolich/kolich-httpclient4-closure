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

package com.kolich.http;

import org.apache.http.client.HttpClient;
import org.apache.http.client.protocol.RequestAcceptEncoding;
import org.apache.http.client.protocol.ResponseContentEncoding;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.impl.conn.SystemDefaultRoutePlanner;

import java.net.ProxySelector;

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.Runtime.getRuntime;

public final class KolichDefaultHttpClient {

    /**
     * The maximum number of total outgoing connections from this
     * {@link KolichDefaultHttpClient} instance.
     */
    private final int maxTotalConnections_;

    /**
     * The maximum number of outgoing connections per route from this
     * {@link KolichDefaultHttpClient} instance.
     */
    private final int maxConnectionsPerRoute_;
	
	public KolichDefaultHttpClient(int maxTotalConnections,
                                   int maxConnectionsPerRoute) {
        checkArgument(maxTotalConnections > 0, "Max total connections " +
            "must be greater than zero.");
		maxTotalConnections_ = maxTotalConnections;
		maxConnectionsPerRoute_ = maxConnectionsPerRoute;
	}
		
	/**
	 * Creates a new {@link HttpClient} using the default set of
	 * parameters, and sets the default proxy selector if asked to.
	 * @param useProxySelector set to true if this client instance
	 * should use the JVM default {@link ProxySelector}.
	 * @return
	 */
	private final HttpClient getNewInstance(final boolean useProxySelector,
                                            final String userAgent) {
        final PoolingHttpClientConnectionManager connectionManager =
            new PoolingHttpClientConnectionManager();
        connectionManager.setDefaultMaxPerRoute(maxConnectionsPerRoute_);
        connectionManager.setMaxTotal(maxTotalConnections_);
        // Establish a builder for this new instance.
        final HttpClientBuilder builder = HttpClients.custom()
            .addInterceptorFirst(new RequestAcceptEncoding())
            .addInterceptorFirst(new ResponseContentEncoding())
            .setConnectionManager(connectionManager)
            .disableAutomaticRetries()
            .disableAuthCaching()
            .setUserAgent(userAgent);
        // Set the default proxy selector, if desired.
        if(useProxySelector) {
            builder.setRoutePlanner(new SystemDefaultRoutePlanner(
                ProxySelector.getDefault()));
        }
        return builder.build();
	}
	
	/**
	 * Returns a new thread safe default {@link HttpClient} instance that
	 * is not pre-configured to understand proxies.
	 * @return
	 */
	public HttpClient getNewInstanceNoProxySelector(final String userAgent) {
		return getNewInstance(false, userAgent);
	}
	
	/**
	 * Returns a new thread safe default {@link HttpClient} instance that
	 * is not pre-configured to understand proxies.  Uses the default
	 * underlying User-Agent of the client library.
	 * @return
	 */
	public HttpClient getNewInstanceNoProxySelector() {
		return getNewInstance(false, null);
	}
	
	/**
	 * Returns a new thread safe default {@link HttpClient} instance that
	 * understands proxies.
	 * @return
	 */
	public HttpClient getNewInstanceWithProxySelector(final String userAgent) {
		return getNewInstance(true, userAgent);
	}
	
	/**
	 * Returns a new thread safe default {@link HttpClient} instance that
	 * understands proxies. Uses the default underlying User-Agent of the
	 * client library.
	 * @return
	 */
	public HttpClient getNewInstanceWithProxySelector() {
		return getNewInstance(true, null);
	}
			
	/**
	 * A class that provides a few static factory methods for Java beans.
	 */
	public final static class KolichHttpClientFactory {
		
		private static final int AVAILABLE_CORES =
			getRuntime().availableProcessors();
		
		// Trying to be like a real browser and only allow at most
		// 15-connections per route by default.
		private static final int DEFAULT_MAX_CONNECTIONS_PER_ROUTE = 15;
		
		// Max total connections defaults to the default max connections
		// per route multiplied by the number of cores.
		private static final int DEFAULT_MAX_TOTAL_CONNECTIONS =
			DEFAULT_MAX_CONNECTIONS_PER_ROUTE * AVAILABLE_CORES;
		
		public static final HttpClient getNewInstanceNoProxySelector(final String userAgent,
                                                                     int maxTotalConnections,
                                                                     int maxConnectionsPerRoute) {
			return new KolichDefaultHttpClient(maxTotalConnections,
				maxConnectionsPerRoute).getNewInstanceNoProxySelector(userAgent);
		}
		
		public static final HttpClient getNewInstanceNoProxySelector(int maxTotalConnections,
                                                                     int maxConnectionsPerRoute) {
			return getNewInstanceNoProxySelector(null,
				maxTotalConnections, maxConnectionsPerRoute);
		}
		
		public static final HttpClient getNewInstanceNoProxySelector(final String userAgent) {
			return getNewInstanceNoProxySelector(userAgent,
				DEFAULT_MAX_TOTAL_CONNECTIONS,
				DEFAULT_MAX_CONNECTIONS_PER_ROUTE);
		}
		
		public static final HttpClient getNewInstanceNoProxySelector() {
			return getNewInstanceNoProxySelector(null);
		}
		
		public static final HttpClient getNewInstanceWithProxySelector(final String userAgent,
                                                                       int maxTotalConnections,
                                                                       int maxConnectionsPerRoute) {
			return new KolichDefaultHttpClient(maxTotalConnections,
				maxConnectionsPerRoute).getNewInstanceWithProxySelector(userAgent);
		}
		
		public static final HttpClient getNewInstanceWithProxySelector(int maxTotalConnections,
                                                                       int maxConnectionsPerRoute) {
			return getNewInstanceWithProxySelector(null,
				maxTotalConnections, maxConnectionsPerRoute);
		}
		
		public static final HttpClient getNewInstanceWithProxySelector(final String userAgent) {
			return getNewInstanceWithProxySelector(userAgent,
				DEFAULT_MAX_TOTAL_CONNECTIONS,
				DEFAULT_MAX_CONNECTIONS_PER_ROUTE);
		}
		
		public static final HttpClient getNewInstanceWithProxySelector() {
			return getNewInstanceWithProxySelector(null);
		}
		
	}
	
}
