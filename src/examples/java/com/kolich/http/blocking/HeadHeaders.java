package com.kolich.http.blocking;

import static com.kolich.http.blocking.KolichDefaultHttpClient.KolichHttpClientFactory.getNewInstanceWithProxySelector;

import org.apache.http.Header;
import org.apache.http.client.HttpClient;

import com.kolich.http.common.either.HttpResponseEither;
import com.kolich.http.common.response.HttpSuccess;

/**
 * Send a HEAD request and expect back an array of HTTP response
 * headers on success. Drop any failures on the floor -- expect a null
 * return value in place of success type S if anything went wrong.
 */
public final class HeadHeaders {

	public static void main(String[] args) {
		
		final HttpClient client = getNewInstanceWithProxySelector();
		
		final HttpResponseEither<Void,Header[]> result =
			new HttpClient4Closure<Void,Header[]>(client) {
			@Override
			public Header[] success(final HttpSuccess success) {
				return success.getResponse().getAllHeaders();
			}
		}.head("http://example.com");

		final Header[] headers = result.right();
		
		for(final Header h : headers) {
			System.out.println(h.getName() + ": " + h.getValue());
		}
		
	}

}
