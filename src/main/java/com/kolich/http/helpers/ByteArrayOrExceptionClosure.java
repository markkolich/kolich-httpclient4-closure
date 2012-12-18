package com.kolich.http.helpers;

import org.apache.http.util.EntityUtils;

import com.kolich.http.helpers.definitions.OrExceptionClosure;

public final class ByteArrayOrExceptionClosure extends OrExceptionClosure<byte[]> {
	
	@Override
	public byte[] success(final HttpSuccess success) throws Exception {
		return EntityUtils.toByteArray(success.getResponse().getEntity());
	}
	
}
