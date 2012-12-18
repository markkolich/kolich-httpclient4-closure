package com.kolich.http.helpers;

import org.apache.http.util.EntityUtils;

import com.kolich.http.helpers.definitions.OrNullClosure;

public final class ByteArrayOrNullClosure extends OrNullClosure<byte[]> {
	
	@Override
	public byte[] success(final HttpSuccess success) throws Exception {
		return EntityUtils.toByteArray(success.getResponse().getEntity());
	}
	
}
