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

import static com.kolich.common.DefaultCharacterEncoding.UTF_8;
import static com.kolich.common.entities.KolichCommonEntity.getDefaultGsonBuilder;
import static org.apache.commons.io.IOUtils.closeQuietly;

import java.io.InputStreamReader;
import java.io.Reader;

import org.apache.http.HttpEntity;

import com.google.gson.Gson;
import com.kolich.http.helpers.definitions.OrExceptionClosure;

public class GsonOrExceptionClosure<S> extends OrExceptionClosure<S> {
	
	private final Gson gson_;
	private final Class<S> clazz_;
	
	public GsonOrExceptionClosure(final Gson gson, final Class<S> clazz) {
		gson_ = gson;
		clazz_ = clazz;
	}
	
	public GsonOrExceptionClosure(final Class<S> clazz) {
		this(getDefaultGsonBuilder().create(), clazz);
	}
	
	@Override
	public final S success(final HttpSuccess success) throws Exception {
		Reader r = null;
		try {
			final HttpEntity entity = success.getResponse().getEntity();
			r = new InputStreamReader(entity.getContent(), UTF_8);
			return gson_.fromJson(r, clazz_);
		} finally {
			closeQuietly(r);
		}
	}
		
}
