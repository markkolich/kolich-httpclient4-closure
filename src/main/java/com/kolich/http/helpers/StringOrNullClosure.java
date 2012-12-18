package com.kolich.http.helpers;

import static com.kolich.common.DefaultCharacterEncoding.UTF_8;

import org.apache.http.util.EntityUtils;

import com.kolich.http.helpers.definitions.OrNullClosure;

public final class StringOrNullClosure extends OrNullClosure<String> {
	
	@Override
	public String success(final HttpSuccess success) throws Exception {
		return EntityUtils.toString(success.getResponse().getEntity(), UTF_8);
	}
	
}
