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

package com.kolich.http;

import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.impl.conn.SystemDefaultRoutePlanner;

import java.net.ProxySelector;

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.Runtime.getRuntime;

public final class HttpClient4ClosureBuilder {

    private static final int AVAILABLE_CORES = getRuntime().availableProcessors();

    // A timeout value of zero is interpreted as an infinite timeout.
    private static final int DEFAULT_INFINITE_TIMEOUT = 0;

    // Trying to be like a real browser and only allow at most
    // 15-connections per route by default.
    private static final int DEFAULT_MAX_CONNECTIONS_PER_ROUTE = 15;

    // Max total connections defaults to the default max connections
    // per route multiplied by the number of cores.
    private static final int DEFAULT_MAX_TOTAL_CONNECTIONS =
        DEFAULT_MAX_CONNECTIONS_PER_ROUTE * AVAILABLE_CORES;

    /**
     * Defines the socket timeout (<code>SO_TIMEOUT</code>) in milliseconds,
     * which is the timeout for waiting for data  or, put differently,
     * a maximum period inactivity between two consecutive data packets).
     * A timeout value of zero is interpreted as an infinite timeout.
     */
    private int socketTimeout_ = DEFAULT_INFINITE_TIMEOUT;

    /**
     * The timeout in milliseconds until a connection is established.
     * A timeout value of zero is interpreted as an infinite timeout.
     */
    private int connectTimeout_ = DEFAULT_INFINITE_TIMEOUT;

    /**
     * The maximum number of total outgoing connections from this
     * {@link HttpClient4ClosureBuilder} instance.
     */
    private int maxTotalConnections_ = DEFAULT_MAX_TOTAL_CONNECTIONS;

    /**
     * The maximum number of outgoing connections per route from this
     * {@link HttpClient4ClosureBuilder} instance.
     */
    private int maxConnectionsPerRoute_ = DEFAULT_MAX_CONNECTIONS_PER_ROUTE;

    private String userAgent_ = null;

    private int validateAfterInactivityMs_ = -1;
    private boolean disableContentCompression_ = true;
    private boolean disableAutomaticRetries_ = true;
    private boolean disableAuthCaching_ = true;

    private boolean useProxySelector_ = true;

	public HttpClient4ClosureBuilder() {}

    public HttpClient4ClosureBuilder setSocketTimeout(final int socketTimeout) {
        socketTimeout_ = socketTimeout;
        return this;
    }

    public HttpClient4ClosureBuilder setConnectTimeout(final int connectTimeout) {
        connectTimeout_ = connectTimeout;
        return this;
    }

    public HttpClient4ClosureBuilder setMaxTotalConnections(final int maxTotalConnections) {
        checkArgument(maxTotalConnections > 0, "Max total connections must be greater than zero.");
        maxTotalConnections_ = maxTotalConnections;
        return this;
    }

    public HttpClient4ClosureBuilder setMaxConnectionsPerRoute(final int maxConnectionsPerRoute) {
        checkArgument(maxConnectionsPerRoute >= 1, "Max connections per route must be positive.");
        maxConnectionsPerRoute_ = maxConnectionsPerRoute;
        return this;
    }

    public HttpClient4ClosureBuilder setUserAgent(final String userAgent) {
        userAgent_ = userAgent;
        return this;
    }

    public HttpClient4ClosureBuilder setValidateAfterInactivityMs(final int validateAfterInactivityMs) {
        validateAfterInactivityMs_ = validateAfterInactivityMs;
        return this;
    }

    public HttpClient4ClosureBuilder disableContentCompression(final boolean disableContentCompression) {
        disableContentCompression_ = disableContentCompression;
        return this;
    }

    public HttpClient4ClosureBuilder disableAutomaticRetries(final boolean disableAutomaticRetries) {
        disableAutomaticRetries_ = disableAutomaticRetries;
        return this;
    }

    public HttpClient4ClosureBuilder disableAuthCaching(final boolean disableAuthCaching) {
        disableAuthCaching_ = disableAuthCaching;
        return this;
    }

    public HttpClient4ClosureBuilder useProxySelector(final boolean useProxySelector) {
        useProxySelector_ = useProxySelector;
        return this;
    }

    /**
     * Creates a new {@link HttpClient} global {@link RequestConfig} object. The {@link RequestConfig} object
     * is where request specific settings like socket and connection timeouts live.
     */
    public RequestConfig getRequestConfig() {
        return RequestConfig.custom()
            .setSocketTimeout(socketTimeout_)
            .setConnectTimeout(connectTimeout_)
            .setConnectionRequestTimeout(connectTimeout_)
            .build();
    }

    public HttpClientConnectionManager getConnectionManager() {
        final PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setDefaultMaxPerRoute(maxConnectionsPerRoute_);
        connectionManager.setMaxTotal(maxTotalConnections_);
        connectionManager.setValidateAfterInactivity(validateAfterInactivityMs_);
        return connectionManager;
    }

    public HttpClientBuilder getHttpClientBuilder() {
        final HttpClientBuilder builder = HttpClients.custom()
            .setDefaultRequestConfig(getRequestConfig())
            .setConnectionManager(getConnectionManager())
            .setUserAgent(userAgent_);
        // See http://stackoverflow.com/questions/21818242/with-httpclient-4-3-x-executing-a-httphead-for-a-specific-url-gives-nohttprespo
        // Also, see https://issues.apache.org/jira/browse/HTTPCLIENT-1464
        // This disables the addition of the `Accept-Encoding: gzip,deflate`
        // header on outgoing requests which seems to confuse some servers.
        // This is off by default, but can be turned off if desired.
        if(disableContentCompression_) {
            builder.disableContentCompression();
        }
        if(disableAutomaticRetries_) {
            builder.disableAutomaticRetries();
        }
        if(disableAuthCaching_) {
            builder.disableAuthCaching();
        }
        if(useProxySelector_) {
            builder.setRoutePlanner(new SystemDefaultRoutePlanner(ProxySelector.getDefault()));
        }
        return builder;
    }

	public HttpClient getNewHttpClientInstance() {
        return getHttpClientBuilder().build();
	}
			
	/**
	 * An inline class that provides a few static factory methods for beans and others who just want a dead
     * simple way to get a working and reasonable {@link HttpClient} instance.
	 */
	public static final class Factory {
		
		public static final HttpClient getNewInstanceNoProxySelector(final String userAgent,
                                                                     int socketTimeout,
                                                                     int connectTimeout,
                                                                     int maxTotalConnections,
                                                                     int maxConnectionsPerRoute) {
            return new HttpClient4ClosureBuilder()
                .setUserAgent(userAgent)
                .setSocketTimeout(socketTimeout)
                .setConnectTimeout(connectTimeout)
                .setMaxTotalConnections(maxTotalConnections)
                .setMaxConnectionsPerRoute(maxConnectionsPerRoute)
                .useProxySelector(false)
                .getNewHttpClientInstance();
		}

        public static final HttpClient getNewInstanceNoProxySelector(int socketTimeout,
                                                                     int connectTimeout,
                                                                     int maxTotalConnections,
                                                                     int maxConnectionsPerRoute) {
            return new HttpClient4ClosureBuilder()
                .setSocketTimeout(socketTimeout)
                .setConnectTimeout(connectTimeout)
                .setMaxTotalConnections(maxTotalConnections)
                .setMaxConnectionsPerRoute(maxConnectionsPerRoute)
                .useProxySelector(false)
                .getNewHttpClientInstance();
        }
		
		public static final HttpClient getNewInstanceNoProxySelector(int maxTotalConnections,
                                                                     int maxConnectionsPerRoute) {
            return new HttpClient4ClosureBuilder()
                .setMaxTotalConnections(maxTotalConnections)
                .setMaxConnectionsPerRoute(maxConnectionsPerRoute)
                .useProxySelector(false)
                .getNewHttpClientInstance();
		}
		
		public static final HttpClient getNewInstanceNoProxySelector(final String userAgent) {
            return new HttpClient4ClosureBuilder()
                .setUserAgent(userAgent)
                .useProxySelector(false)
                .getNewHttpClientInstance();
		}
		
		public static final HttpClient getNewInstanceNoProxySelector() {
            return new HttpClient4ClosureBuilder()
                .useProxySelector(false)
                .getNewHttpClientInstance();
		}
		
		public static final HttpClient getNewInstanceWithProxySelector(final String userAgent,
                                                                       int socketTimeout,
                                                                       int connectTimeout,
                                                                       int maxTotalConnections,
                                                                       int maxConnectionsPerRoute) {
            return new HttpClient4ClosureBuilder()
                .setUserAgent(userAgent)
                .setSocketTimeout(socketTimeout)
                .setConnectTimeout(connectTimeout)
                .setMaxTotalConnections(maxTotalConnections)
                .setMaxConnectionsPerRoute(maxConnectionsPerRoute)
                .getNewHttpClientInstance();
		}
		
		public static final HttpClient getNewInstanceWithProxySelector(int socketTimeout,
                                                                       int connectTimeout,
                                                                       int maxTotalConnections,
                                                                       int maxConnectionsPerRoute) {
            return new HttpClient4ClosureBuilder()
                .setSocketTimeout(socketTimeout)
                .setConnectTimeout(connectTimeout)
                .setMaxTotalConnections(maxTotalConnections)
                .setMaxConnectionsPerRoute(maxConnectionsPerRoute)
                .getNewHttpClientInstance();
		}

        public static final HttpClient getNewInstanceWithProxySelector(int maxTotalConnections,
                                                                       int maxConnectionsPerRoute) {
            return new HttpClient4ClosureBuilder()
                .setMaxTotalConnections(maxTotalConnections)
                .setMaxConnectionsPerRoute(maxConnectionsPerRoute)
                .getNewHttpClientInstance();
        }
		
		public static final HttpClient getNewInstanceWithProxySelector(final String userAgent) {
            return new HttpClient4ClosureBuilder()
                .setUserAgent(userAgent)
                .getNewHttpClientInstance();
		}
		
		public static final HttpClient getNewInstanceWithProxySelector() {
            return new HttpClient4ClosureBuilder()
                .getNewHttpClientInstance();
		}
		
	}
	
}
