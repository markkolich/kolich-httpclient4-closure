package com.kolich.http.async;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.kolich.http.common.either.HttpResponseEither;

public final class AlreadyDoneFuture<F,S> implements Future<HttpResponseEither<F,S>> {
	
	private final HttpResponseEither<F,S> either_;
	
	private AlreadyDoneFuture(final HttpResponseEither<F,S> either) {
		either_ = either;
	}

	@Override
	public boolean cancel(boolean mayInterruptIfRunning) {
		return false;
	}

	@Override
	public boolean isCancelled() {
		return false;
	}

	@Override
	public boolean isDone() {
		return true;
	}

	@Override
	public HttpResponseEither<F,S> get() throws InterruptedException,
		ExecutionException {
		return either_;
	}

	@Override
	public HttpResponseEither<F,S> get(long timeout, TimeUnit unit)
		throws InterruptedException, ExecutionException, TimeoutException {
		return get();
	}
	
	@SuppressWarnings("unchecked") // sigh.
	public static final <F,S,T> Future<T> create(final HttpResponseEither<F,S> either) {
		return (Future<T>) new AlreadyDoneFuture<F,S>(either);
	}
	
}
