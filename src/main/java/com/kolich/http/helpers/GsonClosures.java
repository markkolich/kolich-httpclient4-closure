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

package com.kolich.http.helpers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.kolich.http.common.response.HttpSuccess;
import com.kolich.http.helpers.definitions.OrHttpFailureClosure;
import com.kolich.http.helpers.definitions.OrNullClosure;
import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;

import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;

import static com.kolich.common.DefaultCharacterEncoding.UTF_8;
import static com.kolich.common.entities.KolichCommonEntity.getDefaultGsonBuilder;
import static com.kolich.http.KolichDefaultHttpClient.KolichHttpClientFactory.getNewInstanceWithProxySelector;
import static org.apache.commons.io.IOUtils.closeQuietly;

public final class GsonClosures {
	
	// Cannot instantiate.
	private GsonClosures() {}
	
	public static class GsonOrHttpFailureClosure<S> extends OrHttpFailureClosure<S> {		
		private final Gson gson_;
		private final Type type_;
		private final String charsetName_;		
		public GsonOrHttpFailureClosure(final HttpClient client, final Gson gson,
			final Type type, final String charsetName) {
			super(client);
			gson_ = gson;
			type_ = type;
			charsetName_ = charsetName;
		}
		public GsonOrHttpFailureClosure(final HttpClient client,
			final GsonBuilder builder, final Type type,
			final String charsetName) {
			this(client, builder.create(), type, charsetName);
		}
		public GsonOrHttpFailureClosure(final HttpClient client,
			final Gson gson, final Type type) {
			this(client, gson, type, UTF_8);
		}
		public GsonOrHttpFailureClosure(final HttpClient client,
			final GsonBuilder builder, final Type type) {
			this(client, builder.create(), type, UTF_8);
		}
		public GsonOrHttpFailureClosure(final HttpClient client, final Gson gson,
			final Class<S> clazz, final String charsetName) {
			this(client, gson, TypeToken.get(clazz).getType(), charsetName);
		}
		public GsonOrHttpFailureClosure(final HttpClient client,
			final GsonBuilder builder, final Class<S> clazz,
			final String charsetName) {
			this(client, builder.create(), TypeToken.get(clazz).getType(),
				charsetName);
		}
		public GsonOrHttpFailureClosure(final HttpClient client,
			final Gson gson, final Class<S> clazz) {
			this(client, gson, clazz, UTF_8);
		}
		public GsonOrHttpFailureClosure(final HttpClient client,
			final GsonBuilder builder, final Class<S> clazz) {
			this(client, builder.create(), clazz, UTF_8);
		}
		public GsonOrHttpFailureClosure(final HttpClient client,
			final Type type, final String charsetName) {
			this(client, getDefaultGsonBuilder().create(), type, charsetName);
		}
		public GsonOrHttpFailureClosure(final HttpClient client,
			final Type type) {
			this(client, type, UTF_8);
		}
		public GsonOrHttpFailureClosure(final HttpClient client,
			final Class<S> clazz) {
			this(client, getDefaultGsonBuilder().create(), clazz);
		}
		public GsonOrHttpFailureClosure(final Type type,
			final String charsetName) {
			this(getNewInstanceWithProxySelector(), type, charsetName);
		}
		public GsonOrHttpFailureClosure(final Type type) {
			this(getNewInstanceWithProxySelector(), type);
		}
		public GsonOrHttpFailureClosure(final Class<S> clazz,
			final String charsetName) {
			this(getNewInstanceWithProxySelector(), clazz, charsetName);
		}
		public GsonOrHttpFailureClosure(final Class<S> clazz) {
			this(getNewInstanceWithProxySelector(), clazz);
		}
		@Override
		public final S success(final HttpSuccess success) throws Exception {
			Reader r = null;
			try {
				final HttpEntity entity = success.getResponse().getEntity();
				r = new InputStreamReader(entity.getContent(), charsetName_);
				return gson_.fromJson(r, type_);
			} finally {
				closeQuietly(r);
			}
		}
	}
	
	public static class GsonOrNullClosure<S> extends OrNullClosure<S> {		
		private final Gson gson_;
		private final Type type_;
		private final String charsetName_;		
		public GsonOrNullClosure(final HttpClient client, final Gson gson,
			final Type type, final String charsetName) {
			super(client);
			gson_ = gson;
			type_ = type;
			charsetName_ = charsetName;
		}
		public GsonOrNullClosure(final HttpClient client,
			final GsonBuilder builder, final Type type,
			final String charsetName) {
			this(client, builder.create(), type, charsetName);
		}
		public GsonOrNullClosure(final HttpClient client,
			final Gson gson, final Type type) {
			this(client, gson, type, UTF_8);
		}
		public GsonOrNullClosure(final HttpClient client,
			final GsonBuilder builder, final Type type) {
			this(client, builder.create(), type, UTF_8);
		}
		public GsonOrNullClosure(final HttpClient client, final Gson gson,
			final Class<S> clazz, final String charsetName) {
			this(client, gson, TypeToken.get(clazz).getType(), charsetName);
		}
		public GsonOrNullClosure(final HttpClient client,
			final GsonBuilder builder, final Class<S> clazz,
			final String charsetName) {
			this(client, builder.create(), TypeToken.get(clazz).getType(),
				charsetName);
		}
		public GsonOrNullClosure(final HttpClient client,
			final Gson gson, final Class<S> clazz) {
			this(client, gson, clazz, UTF_8);
		}
		public GsonOrNullClosure(final HttpClient client,
			final GsonBuilder builder, final Class<S> clazz) {
			this(client, builder.create(), clazz, UTF_8);
		}
		public GsonOrNullClosure(final HttpClient client,
			final Type type, final String charsetName) {
			this(client, getDefaultGsonBuilder().create(), type, charsetName);
		}
		public GsonOrNullClosure(final HttpClient client,
			final Type type) {
			this(client, type, UTF_8);
		}
		public GsonOrNullClosure(final HttpClient client,
			final Class<S> clazz) {
			this(client, getDefaultGsonBuilder().create(), clazz);
		}
		public GsonOrNullClosure(final Type type,
			final String charsetName) {
			this(getNewInstanceWithProxySelector(), type, charsetName);
		}
		public GsonOrNullClosure(final Type type) {
			this(getNewInstanceWithProxySelector(), type);
		}
		public GsonOrNullClosure(final Class<S> clazz,
			final String charsetName) {
			this(getNewInstanceWithProxySelector(), clazz, charsetName);
		}
		public GsonOrNullClosure(final Class<S> clazz) {
			this(getNewInstanceWithProxySelector(), clazz);
		}
		@Override
		public final S success(final HttpSuccess success) throws Exception {
			Reader r = null;
			try {
				final HttpEntity entity = success.getResponse().getEntity();
				r = new InputStreamReader(entity.getContent(), charsetName_);
				return gson_.fromJson(r, type_);
			} finally {
				closeQuietly(r);
			}
		}
	}

}
