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

package com.kolich.http;

import java.net.ProxySelector;

import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.protocol.RequestAcceptEncoding;
import org.apache.http.client.protocol.ResponseContentEncoding;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.impl.conn.ProxySelectorRoutePlanner;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;

public final class KolichDefaultHttpClient {
	
	private static final String HTTP_USERAGENT_PARAM = "http.useragent";
	
	/**
	 * The maximum number of total connections, across all routes/hosts.
	 */
	private static final int DEFAULT_MAX_TOTAL_CONNECTIONS = 200;
	
	/**
	 * The maximum number of connections we can have to any given host.
	 */
	private static final int DEFAULT_MAX_CONNECTIONS_PER_ROUTE = 10;
	
	private static final int DEFAULT_CONNECTION_TIMEOUT_MS = 10000; // 10 secs
	private static final int DEFAULT_SOCKET_TIMEOUT_MS = 0; // Inf
		
	private int maxTotalConnections_ = DEFAULT_MAX_TOTAL_CONNECTIONS;
	
	private int maxConnectionsPerRoute_ = DEFAULT_MAX_CONNECTIONS_PER_ROUTE;
	
	/**
	 * Determines the timeout in milliseconds until a connection is
	 * established.  A timeout value of zero is interpreted as an
	 * infinite timeout.
	 */
	private int connectionTimeoutMs_ = DEFAULT_CONNECTION_TIMEOUT_MS;
	
	/**
	 * Defines the socket timeout (SO_TIMEOUT) in milliseconds, which
	 * is the timeout for waiting for data.  A timeout value of zero
	 * is interpreted as an infinite timeout.
	 */
	private int socketTimoutMs_ = DEFAULT_SOCKET_TIMEOUT_MS;
		
	/**
	 * Creates a new {@link HttpClient} using the default set of
	 * parameters, and sets the default proxy selector if asked to.
	 * @param useProxySelector set to true if this client instance
	 * should use the JVM default {@link ProxySelector}.
	 * @return
	 */
	private final HttpClient getNewInstance(final boolean useProxySelector,
		final String userAgent) {
		// Set some parameters.
		final HttpParams params = new BasicHttpParams();
		HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
		// Set the socket and connection timeout accordingly.
		HttpConnectionParams.setConnectionTimeout(params, connectionTimeoutMs_);
		HttpConnectionParams.setSoTimeout(params, socketTimoutMs_);
		// Determines whether stale connection check is to be used. The
		// stale connection check can cause up to 30 millisecond overhead per
		// request and should be used only when appropriate. For performance
		// critical operations this check should be disabled.
		HttpConnectionParams.setStaleCheckingEnabled(params, false);
		// Determines whether Nagle's algorithm is to be used. The Nagle's
		// algorithm tries to conserve bandwidth by minimizing the number of
		// segments that are sent. When applications wish to decrease network
		// latency and increase performance, they can disable Nagle's
		// algorithm (that is enable TCP_NODELAY). Data will be sent earlier,
		// at the cost of an increase in bandwidth consumption.
		HttpConnectionParams.setTcpNoDelay(params, true);
		// Set the HTTP User-Agent parameter too if one was set by the consumer.
		if(userAgent != null) {
			params.setParameter(HTTP_USERAGENT_PARAM, userAgent);
		}
		// PoolingClientConnectionManager is a more complex implementation that
		// manages a pool of client connections and is able to service
		// connection requests from multiple execution threads. Connections
		// are pooled on a per route basis. A request for a route for which
		// the manager already has a persistent connection available in the
		// pool will be serviced by leasing a connection from the pool rather
		// than creating a brand new connection.
		final PoolingClientConnectionManager cm =
			new PoolingClientConnectionManager();
		// Set the max connections per route and the maximum number of
		// total connections this connection manager is allowed to use.
		cm.setMaxTotal(maxTotalConnections_);
		cm.setDefaultMaxPerRoute(maxConnectionsPerRoute_);
		final HttpClient client = getInstance(params, cm);
		// Declare support for GZIP and DEFLATE response encodings.
		((DefaultHttpClient)client).addRequestInterceptor(
			new RequestAcceptEncoding());
		((DefaultHttpClient)client).addResponseInterceptor(
			new ResponseContentEncoding());
		// Setup this new client with a default ProxySelector which
		// obeys the -Dhttp[s].proxyHost and -Dhttp[s].proxyPort VM args.
		if(useProxySelector) {
			// If we were asked to use a proxy selector, then set one.
			((DefaultHttpClient)client).setRoutePlanner(
				new ProxySelectorRoutePlanner(
					client.getConnectionManager().getSchemeRegistry(),
						ProxySelector.getDefault()));
		}
		return client;
	}
	
	/**
	 * Instantiate a new {@link HttpClient} instance using the provided
	 * {@link HttpParams} and requested {@link ClientConnectionManager}.
	 * @return
	 */
	private HttpClient getInstance(final HttpParams params,
		final ClientConnectionManager manager) {
		return new DefaultHttpClient(manager, params);
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
	
	public KolichDefaultHttpClient setMaxTotalConnections(int maxTotalConnections) {
		maxTotalConnections_ = maxTotalConnections;
		return this;
	}
	
	public KolichDefaultHttpClient setMaxConnectionsPerRoute(int maxConnectionsPerRoute) {
		maxConnectionsPerRoute_ = maxConnectionsPerRoute;
		return this;
	}
	
	public KolichDefaultHttpClient setConnectionTimeoutMs(int connectionTimeoutMs) {
		connectionTimeoutMs_ = connectionTimeoutMs;
		return this;
	}
	
	public KolichDefaultHttpClient setSocketTimeoutMs(int socketTimoutMs) {
		socketTimoutMs_ = socketTimoutMs;
		return this;
	}
		
	/**
	 * A class that provides a few static factory methods for Spring Beans.
	 */
	public final static class KolichHttpClientFactory {
		
		public static final HttpClient getNewInstanceNoProxySelector(
			final String userAgent) {
			return new KolichDefaultHttpClient()
				.getNewInstanceNoProxySelector(userAgent);
		}
		
		public static final HttpClient getNewInstanceNoProxySelector() {
			return getNewInstanceNoProxySelector(null);
		}
		
		public static final HttpClient getNewInstanceWithProxySelector(
			final String userAgent) {
			return new KolichDefaultHttpClient()
				.getNewInstanceWithProxySelector(userAgent);
		}
		
		public static final HttpClient getNewInstanceWithProxySelector() {
			return getNewInstanceWithProxySelector(null);
		}
		
	}
	
}
