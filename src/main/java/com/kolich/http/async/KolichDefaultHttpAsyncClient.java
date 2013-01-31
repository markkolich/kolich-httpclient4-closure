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

package com.kolich.http.async;

import java.net.ProxySelector;

import org.apache.http.HttpVersion;
import org.apache.http.client.protocol.RequestAcceptEncoding;
import org.apache.http.client.protocol.ResponseContentEncoding;
import org.apache.http.impl.nio.client.AbstractHttpAsyncClient;
import org.apache.http.impl.nio.client.DefaultHttpAsyncClient;
import org.apache.http.impl.nio.conn.PoolingClientAsyncConnectionManager;
import org.apache.http.impl.nio.reactor.DefaultConnectingIOReactor;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.nio.client.HttpAsyncClient;
import org.apache.http.nio.conn.ClientAsyncConnectionManager;
import org.apache.http.nio.reactor.ConnectingIOReactor;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;

import com.kolich.http.async.routing.ProxySelectorAsyncRoutePlanner;
import com.kolich.http.exceptions.HttpClient4ClosureException;

public final class KolichDefaultHttpAsyncClient {
	
	private static final String HTTP_USERAGENT_PARAM = "http.useragent";
	
	/**
	 * The default maximum number of total connections, across all
	 * routes/hosts.
	 */
	private static final int DEFAULT_MAX_TOTAL_CONNECTIONS = 200;
	
	/**
	 * The default maximum number of connections we can have to
	 * any given host.  Assumes the client would be connecting to at
	 * most (2) hosts/routes in typical situations.
	 */
	private static final int DEFAULT_MAX_CONNECTIONS_PER_ROUTE = 100;
	
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
	 * Creates a new async {@link HttpAsyncClient} using the default set
	 * of parameters, and sets the default proxy selector if asked to.
	 * @param useProxySelector set to true if this client instance
	 * should use the JVM default {@link ProxySelector}.
	 * @return
	 */
	private final HttpAsyncClient getNewAsyncInstance(
		final boolean useProxySelector, final String userAgent) {
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
		try {
			// Create a connection manager with some default I/O reactor
			// configuration policies.
			final IOReactorConfig config = new IOReactorConfig();		
			final ConnectingIOReactor reactor =
				new DefaultConnectingIOReactor(config);		
			final PoolingClientAsyncConnectionManager cm =
				new PoolingClientAsyncConnectionManager(reactor);		
			// Set the max connections per route and the maximum number of
			// total connections this connection manager is allowed to use.
			cm.setMaxTotal(maxTotalConnections_);
			cm.setDefaultMaxPerRoute(maxConnectionsPerRoute_);
			final HttpAsyncClient client = getAsyncInstance(params, cm);		
			// Declare support for GZIP and DEFLATE response encodings.
			((DefaultHttpAsyncClient)client).addRequestInterceptor(
				new RequestAcceptEncoding());
			((DefaultHttpAsyncClient)client).addResponseInterceptor(
				new ResponseContentEncoding());
			// Setup this new client with a default ProxySelector which
			// obeys the -Dhttp[s].proxyHost and -Dhttp[s].proxyPort VM args.
			if(useProxySelector) {
				final ProxySelectorAsyncRoutePlanner routePlanner =
					new ProxySelectorAsyncRoutePlanner(
						client.getConnectionManager().getSchemeRegistry(),
						ProxySelector.getDefault());
				// If we were asked to use a proxy selector, then set one.
				((DefaultHttpAsyncClient)client).setRoutePlanner(routePlanner);
			}
			return client;
		} catch (Exception e) {
			throw new HttpClient4ClosureException(e);
		}
	}
		
	/**
	 * Instantiate a new async {@link HttpAsyncClient} instance using the
	 * provided {@link HttpParams} and requested
	 * {@link ClientAsyncConnectionManager}.
	 * @return
	 */
	private HttpAsyncClient getAsyncInstance(final HttpParams params,
		final ClientAsyncConnectionManager manager) {
		final HttpAsyncClient client = new DefaultHttpAsyncClient(manager);
		((AbstractHttpAsyncClient)client).setParams(params);
		return client;
	}
	
	/**
	 * Returns a new thread safe default {@link HttpAsyncClient} instance that
	 * is not pre-configured to understand proxies.
	 * @return
	 */
	public HttpAsyncClient getNewAsyncInstanceNoProxySelector(
		final String userAgent) {
		return getNewAsyncInstance(false, userAgent);
	}
	
	/**
	 * Returns a new thread safe default {@link HttpAsyncClient} instance that
	 * is not pre-configured to understand proxies.  Uses the default
	 * underlying User-Agent of the client library.
	 * @return
	 */
	public HttpAsyncClient getNewAsyncInstanceNoProxySelector() {
		return getNewAsyncInstance(false, null);
	}
	
	/**
	 * Returns a new thread safe default {@link HttpAsyncClient} instance that
	 * understands proxies.
	 * @return
	 */
	public HttpAsyncClient getNewAsyncInstanceWithProxySelector(
		final String userAgent) {
		return getNewAsyncInstance(true, userAgent);
	}
	
	/**
	 * Returns a new thread safe default {@link HttpAsyncClient} instance that
	 * understands proxies. Uses the default underlying User-Agent of the
	 * client library.
	 * @return
	 */
	public HttpAsyncClient getNewAsyncInstanceWithProxySelector() {
		return getNewAsyncInstance(true, null);
	}
	
	public KolichDefaultHttpAsyncClient setMaxTotalConnections(int maxTotalConnections) {
		maxTotalConnections_ = maxTotalConnections;
		return this;
	}
	
	public KolichDefaultHttpAsyncClient setMaxConnectionsPerRoute(int maxConnectionsPerRoute) {
		maxConnectionsPerRoute_ = maxConnectionsPerRoute;
		return this;
	}
	
	public KolichDefaultHttpAsyncClient setConnectionTimeoutMs(int connectionTimeoutMs) {
		connectionTimeoutMs_ = connectionTimeoutMs;
		return this;
	}
	
	public KolichDefaultHttpAsyncClient setSocketTimeoutMs(int socketTimoutMs) {
		socketTimoutMs_ = socketTimoutMs;
		return this;
	}
		
	/**
	 * A class that provides a few static factory methods for Spring Beans.
	 */
	public final static class KolichHttpClientFactory {
		
		public static final HttpAsyncClient getNewAsyncInstanceNoProxySelector(
			final String userAgent) {
			return new KolichDefaultHttpAsyncClient()
				.getNewAsyncInstanceNoProxySelector(userAgent);
		}
		
		public static final HttpAsyncClient getNewAsyncInstanceNoProxySelector() {
			return getNewAsyncInstanceNoProxySelector(null);
		}
		
		public static final HttpAsyncClient getNewAsyncInstanceWithProxySelector(
			final String userAgent) {
			return new KolichDefaultHttpAsyncClient()
				.getNewAsyncInstanceWithProxySelector(userAgent);
		}
		
		public static final HttpAsyncClient getNewAsyncInstanceWithProxySelector() {
			return getNewAsyncInstanceWithProxySelector(null);
		}
		
	}
	
}
