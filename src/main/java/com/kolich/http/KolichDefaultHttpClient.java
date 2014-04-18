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
import org.apache.http.client.config.RequestConfig;
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
     * Defines the socket timeout (<code>SO_TIMEOUT</code>) in milliseconds,
     * which is the timeout for waiting for data  or, put differently,
     * a maximum period inactivity between two consecutive data packets).
     * A timeout value of zero is interpreted as an infinite timeout.
     */
    private final int socketTimeout_;

    /**
     * The timeout in milliseconds until a connection is established.
     * A timeout value of zero is interpreted as an infinite timeout.
     */
    private final int connectTimeout_;

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
	
	public KolichDefaultHttpClient(int socketTimeout,
                                   int connectTimeout,
                                   int maxTotalConnections,
                                   int maxConnectionsPerRoute) {
        socketTimeout_ = socketTimeout;
        connectTimeout_ = connectTimeout;
        checkArgument(maxTotalConnections > 0, "Max total connections " +
            "must be greater than zero.");
		maxTotalConnections_ = maxTotalConnections;
		maxConnectionsPerRoute_ = maxConnectionsPerRoute;
	}

    public KolichDefaultHttpClient(int maxTotalConnections,
                                   int maxConnectionsPerRoute) {
        // Timeout values of zero are interpreted as an infinite timeout.
        this(0, 0, maxTotalConnections, maxConnectionsPerRoute);
    }

    /**
     * Creates a new {@link HttpClient} global {@link RequestConfig} object.
     * The {@link RequestConfig} object is where request specific settings
     * like socket and connection timeouts live.
     */
    private final RequestConfig getRequestConfig() {
        return RequestConfig.custom()
            .setSocketTimeout(socketTimeout_)
            .setConnectTimeout(connectTimeout_)
            .setConnectionRequestTimeout(connectTimeout_)
            // When this is set to false, this apparently causes bugs
            // with the PoolingHttpClientConnectionManager in 4.3.3. It's
            // unclear to me why, but I'm leaving this enabled although
            // claims are made it adds 30ms of lag time to every request.
            .setStaleConnectionCheckEnabled(true)
            .build();
    }
		
	/**
	 * Creates a new {@link HttpClient} using the default set of
	 * parameters, and sets the default proxy selector if asked to.
	 * @param useProxySelector set to true if this client instance
	 * should use the JVM default {@link ProxySelector}.
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
            .setUserAgent(userAgent)
            .setDefaultRequestConfig(getRequestConfig());
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
	 */
	public HttpClient getNewInstanceNoProxySelector(final String userAgent) {
		return getNewInstance(false, userAgent);
	}
	
	/**
	 * Returns a new thread safe default {@link HttpClient} instance that
	 * is not pre-configured to understand proxies.  Uses the default
	 * underlying User-Agent of the client library.
	 */
	public HttpClient getNewInstanceNoProxySelector() {
		return getNewInstance(false, null);
	}
	
	/**
	 * Returns a new thread safe default {@link HttpClient} instance that
	 * understands proxies.
	 */
	public HttpClient getNewInstanceWithProxySelector(final String userAgent) {
		return getNewInstance(true, userAgent);
	}
	
	/**
	 * Returns a new thread safe default {@link HttpClient} instance that
	 * understands proxies. Uses the default underlying User-Agent of the
	 * client library.
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

        // A timeout value of zero is interpreted as an infinite timeout.
        private static final int DEFAULT_INFINITE_TIMEOUT = 0;

		// Trying to be like a real browser and only allow at most
		// 15-connections per route by default.
		private static final int DEFAULT_MAX_CONNECTIONS_PER_ROUTE = 15;
		
		// Max total connections defaults to the default max connections
		// per route multiplied by the number of cores.
		private static final int DEFAULT_MAX_TOTAL_CONNECTIONS =
			DEFAULT_MAX_CONNECTIONS_PER_ROUTE * AVAILABLE_CORES;
		
		public static final HttpClient getNewInstanceNoProxySelector(final String userAgent,
                                                                     int socketTimeout,
                                                                     int connectTimeout,
                                                                     int maxTotalConnections,
                                                                     int maxConnectionsPerRoute) {
			return new KolichDefaultHttpClient(
                    socketTimeout, connectTimeout,
                    maxTotalConnections, maxConnectionsPerRoute)
                .getNewInstanceNoProxySelector(userAgent);
		}

        public static final HttpClient getNewInstanceNoProxySelector(int socketTimeout,
                                                                     int connectTimeout,
                                                                     int maxTotalConnections,
                                                                     int maxConnectionsPerRoute) {
            return getNewInstanceNoProxySelector(null,
                socketTimeout, connectTimeout,
                maxTotalConnections, maxConnectionsPerRoute);
        }
		
		public static final HttpClient getNewInstanceNoProxySelector(int maxTotalConnections,
                                                                     int maxConnectionsPerRoute) {
			return getNewInstanceNoProxySelector(
                DEFAULT_INFINITE_TIMEOUT, DEFAULT_INFINITE_TIMEOUT,
				maxTotalConnections, maxConnectionsPerRoute);
		}
		
		public static final HttpClient getNewInstanceNoProxySelector(final String userAgent) {
			return getNewInstanceNoProxySelector(userAgent,
                DEFAULT_INFINITE_TIMEOUT, DEFAULT_INFINITE_TIMEOUT,
				DEFAULT_MAX_TOTAL_CONNECTIONS, DEFAULT_MAX_CONNECTIONS_PER_ROUTE);
		}
		
		public static final HttpClient getNewInstanceNoProxySelector() {
			return getNewInstanceNoProxySelector(null);
		}
		
		public static final HttpClient getNewInstanceWithProxySelector(final String userAgent,
                                                                       int socketTimeout,
                                                                       int connectTimeout,
                                                                       int maxTotalConnections,
                                                                       int maxConnectionsPerRoute) {
            return new KolichDefaultHttpClient(
                    socketTimeout, connectTimeout,
                    maxTotalConnections, maxConnectionsPerRoute)
                .getNewInstanceWithProxySelector(userAgent);
		}
		
		public static final HttpClient getNewInstanceWithProxySelector(int socketTimeout,
                                                                       int connectTimeout,
                                                                       int maxTotalConnections,
                                                                       int maxConnectionsPerRoute) {
			return getNewInstanceWithProxySelector(null,
                socketTimeout, connectTimeout,
				maxTotalConnections, maxConnectionsPerRoute);
		}

        public static final HttpClient getNewInstanceWithProxySelector(int maxTotalConnections,
                                                                       int maxConnectionsPerRoute) {
            return getNewInstanceWithProxySelector(
                DEFAULT_INFINITE_TIMEOUT, DEFAULT_INFINITE_TIMEOUT,
                maxTotalConnections, maxConnectionsPerRoute);
        }
		
		public static final HttpClient getNewInstanceWithProxySelector(final String userAgent) {
			return getNewInstanceWithProxySelector(userAgent,
                DEFAULT_INFINITE_TIMEOUT, DEFAULT_INFINITE_TIMEOUT,
				DEFAULT_MAX_TOTAL_CONNECTIONS, DEFAULT_MAX_CONNECTIONS_PER_ROUTE);
		}
		
		public static final HttpClient getNewInstanceWithProxySelector() {
			return getNewInstanceWithProxySelector(null);
		}
		
	}
	
}
