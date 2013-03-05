package com.kolich.http.blocking;

import static com.kolich.common.DefaultCharacterEncoding.UTF_8;
import static com.kolich.http.blocking.KolichDefaultHttpClient.KolichHttpClientFactory.getNewInstanceWithProxySelector;

import org.apache.http.client.HttpClient;
import org.apache.http.util.EntityUtils;

import com.kolich.http.common.either.HttpResponseEither;
import com.kolich.http.common.response.HttpFailure;
import com.kolich.http.common.response.HttpSuccess;

/**
 * Send a GET request expecting either a String back on success, or an
 * Exception on failure -- this is represented by the
 * HttpResponseEither&lt;Exception,String&gt; return type.
 */
public final class GetBodyAsString {

	public static void main(String[] args) {

		final HttpClient client = getNewInstanceWithProxySelector();

		final HttpResponseEither<Exception,String> result =
			new HttpClient4Closure<Exception,String>(client) {
			@Override
			public String success(final HttpSuccess success) throws Exception {
				return EntityUtils.toString(success.getEntity(), UTF_8);
			}
			@Override
			public Exception failure(final HttpFailure failure) {
				return failure.getCause();
			}
		}.get("http://example.com");
		
		if(result.success()) {
			System.out.println("Worked! I have a string that is " +
				result.right().length() + " chars long.");
		} else {
			System.out.println("Failed miserably: " + result.left());
		}

	}

}
