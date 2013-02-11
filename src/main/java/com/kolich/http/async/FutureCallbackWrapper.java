package com.kolich.http.async;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.apache.http.HttpResponse;
import org.apache.http.concurrent.FutureCallback;

import com.kolich.http.common.either.HttpResponseEither;

public abstract class FutureCallbackWrapper<F,S> implements FutureCallback<HttpResponse> {
	
	private final ExecutorService pool_;
	
	private Future<HttpResponseEither<F,S>> future_ = null;
	
	public FutureCallbackWrapper(final ExecutorService pool) {
		pool_ = pool;
	}
	
	@Override
	public final void completed(final HttpResponse response) {
		future_ = pool_.submit(new Callable<HttpResponseEither<F,S>>() {
			@Override
			public HttpResponseEither<F,S> call() throws Exception {
				return onComplete(response);
			}
		});
	}
	
    @Override
	public final void failed(final Exception e) {
    	future_ = pool_.submit(new Callable<HttpResponseEither<F,S>>() {
			@Override
			public HttpResponseEither<F,S> call() throws Exception {
				return onFailure(e);
			}
		});
    }

    @Override
	public final void cancelled() {    	
    	future_ = pool_.submit(new Callable<HttpResponseEither<F,S>>() {
			@Override
			public HttpResponseEither<F,S> call() throws Exception {
				return onCancel();
			}
		});
    }
    
    public abstract HttpResponseEither<F,S> onComplete(final HttpResponse response);
    
    public abstract HttpResponseEither<F,S> onFailure(final Exception e);
    
    public abstract HttpResponseEither<F,S> onCancel();
    
    public final Future<HttpResponseEither<F,S>> getFuture() {
    	return future_;
    }

}
