package com.kolich.http.async;

import org.apache.http.HttpResponse;
import org.apache.http.concurrent.FutureCallback;

import com.kolich.http.common.either.HttpResponseEither;

public abstract class FutureCallbackWrapper<F,S> implements FutureCallback<HttpResponse> {
	
	private HttpResponseEither<F,S> result_ = null;
	
	@Override
	public final void completed(final HttpResponse response) {
		result_ = onComplete(response);
	}
	
    @Override
	public final void failed(final Exception e) {
    	result_ = onFailure(e);
    }

    @Override
	public final void cancelled() {
    	result_ = onCancel();
    }
    
    public abstract HttpResponseEither<F,S> onComplete(final HttpResponse response);
    
    public abstract HttpResponseEither<F,S> onFailure(final Exception e);
    
    public abstract HttpResponseEither<F,S> onCancel();
    
    public final HttpResponseEither<F,S> getEither() {
    	return result_;
    }

}
