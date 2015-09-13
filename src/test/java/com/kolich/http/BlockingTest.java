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

import com.kolich.common.functional.either.Either;
import com.kolich.http.common.response.HttpFailure;
import com.kolich.http.common.response.HttpSuccess;
import com.kolich.http.helpers.StringClosures.StringOrNullClosure;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.List;

import static com.kolich.common.DefaultCharacterEncoding.UTF_8;
import static com.kolich.http.HttpClient4ClosureBuilder.Factory.getNewInstanceWithProxySelector;
import static org.apache.http.client.protocol.HttpClientContext.COOKIE_STORE;

public final class BlockingTest {
	
	public static void main(String[] args) {
				
		final HttpClient client = getNewInstanceWithProxySelector("foobar");
		
		final Either<Integer,String> result = new HttpClient4Closure<Integer,String>(client) {
			@Override
			public void before(final HttpRequestBase request) {
				request.addHeader("Authorization", "super-secret-password");
			}
			@Override
			public String success(final HttpSuccess success) throws Exception {
				return EntityUtils.toString(success.getResponse().getEntity(), UTF_8);
			}
			@Override
			public Integer failure(final HttpFailure failure) {
				return failure.getStatusCode();
			}
		}.get("http://google.com");
		if(result.success()) {
			System.out.println(result.right());
		} else {
			System.out.println(result.left());
		}
		
		final Either<Void,Header[]> hResult = new HttpClient4Closure<Void,Header[]>(client) {
			@Override
			public Header[] success(final HttpSuccess success) throws Exception {
				return success.getResponse().getAllHeaders();
			}
		}.head("http://example.com");
		if(hResult.success()) {
			System.out.println("Fetched " + hResult.right().length + " request headers.");
		}
		
		final Either<Void,String> sResult =
			new StringOrNullClosure(client)
				.get("http://mark.koli.ch");
		if(sResult.success()) {
			System.out.println(sResult.right());
		} else {
			System.out.println(sResult.left());
		}
		
		final Either<Exception,String> eResult = new HttpClient4Closure<Exception,String>(client) {
			@Override
			public String success(final HttpSuccess success) throws Exception {
				return EntityUtils.toString(success.getResponse().getEntity(), UTF_8);
			}
			@Override
			public Exception failure(final HttpFailure failure) {
				return failure.getCause();
			}
		}.put("http://lskdjflksdfjslkf.jfjkfhddfgsdfsdf.com");
		if(!eResult.success()) {
			System.out.println(eResult.left());
		}
		
		// Custom check for "success".
		final Either<Exception,String> cResult = new HttpClient4Closure<Exception,String>(client) {
			@Override
			public boolean check(final HttpResponse response, final HttpContext context) {
				return (response.getStatusLine().getStatusCode() == 405);
			}
			@Override
			public String success(final HttpSuccess success) throws Exception {
				return EntityUtils.toString(success.getResponse().getEntity(), UTF_8);
			}
		}.put("http://google.com");
		if(cResult.success()) {
			System.out.println(cResult.right());
		}
		
		final Either<Exception,OutputStream> bResult = new HttpClient4Closure<Exception,OutputStream>(client) {
			@Override
			public OutputStream success(final HttpSuccess success) throws Exception {
				final OutputStream os = new ByteArrayOutputStream();
				IOUtils.copy(success.getResponse().getEntity().getContent(), os);
				return os;
			}
			@Override
			public Exception failure(final HttpFailure failure) {
				return failure.getCause();
			}
		}.get("http://google.com");
		if(bResult.success()) {
			System.out.println("Loaded bytes into output stream!");
		}
		
		final OutputStream os = new ByteArrayOutputStream();
		final Either<Exception,Integer> stResult = new HttpClient4Closure<Exception,Integer>(client) {
			@Override
			public Integer success(final HttpSuccess success) throws Exception {
				return IOUtils.copy(success.getResponse().getEntity().getContent(), os);
			}
			/*
			@Override
			public Exception failure(final HttpFailure failure) {
				return failure.getCause();
			}
			*/
		}.get("http://mark.koli.ch");
		if(stResult.success()) {
			System.out.println("Loaded " + stResult.right() + " bytes.");
		}
		
		/*
		final HttpContext context = new BasicHttpContext();
		// Setup a basic cookie store so that the response can fetch
		// any cookies returned by the server in the response.
		context.setAttribute(COOKIE_STORE, new BasicCookieStore());
		final Either<Void,String> cookieResult =
			new HttpClientClosureExpectString(client)
				.get(new HttpGet("http://google.com"), context);
		if(cookieResult.success()) {
			// List out all cookies that came back from Google in the response.
			final CookieStore cookies = (CookieStore)context.getAttribute(COOKIE_STORE);
			for(final Cookie c : cookies.getCookies()) {
				System.out.println(c.getName() + " -> " + c.getValue());
			}
		}*/
		
		final Either<Integer,List<Cookie>> mmmmm =
			new HttpClient4Closure<Integer,List<Cookie>>(client) {
			@Override
			public void before(final HttpRequestBase request, final HttpContext context) {
				context.setAttribute(COOKIE_STORE, new BasicCookieStore());
			}
			@Override
			public List<Cookie> success(final HttpSuccess success) {
				// Extract a list of cookies from the request.
				// Might be empty.
				return ((CookieStore)success.getContext()
					.getAttribute(COOKIE_STORE)).getCookies();
			}
			@Override
			public Integer failure(final HttpFailure failure) {
				return failure.getStatusCode();
			}
		}.get("http://google.com");
		final List<Cookie> cookies;
		if((cookies = mmmmm.right()) != null) {
			for(final Cookie c : cookies) {
				System.out.println(c.getName() + " -> " + c.getValue());
			}
		} else {
			System.out.println("Failed miserably: " + mmmmm.left());
		}
		
		final Either<Void,Header[]> haResult =
			new HttpClient4Closure<Void, Header[]>(client) {
			@Override
			public Header[] success(final HttpSuccess success) {
				return success.getResponse().getAllHeaders();
			}
		}.head("http://java.com");
		final Header[] headers = haResult.right();
		if(headers != null) {
			for(final Header h : headers) {
				System.out.println(h.getName() + ": " + h.getValue());
			}
		}
				
	}
	
}
