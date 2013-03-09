/**
 * Copyright (c) 2013 Mark S. Kolich
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

package com.kolich.http.blocking;

import static com.kolich.http.common.response.ResponseUtils.consumeQuietly;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.protocol.HttpContext;

import com.kolich.common.either.Either;
import com.kolich.common.either.Left;
import com.kolich.common.either.Right;
import com.kolich.http.common.HttpClient4ClosureBase;
import com.kolich.http.common.response.HttpFailure;
import com.kolich.http.common.response.HttpSuccess;

public abstract class HttpClient4Closure<F,S>
	extends HttpClient4ClosureBase<Either<F,S>> {
				
	private final HttpClient client_;
	
	public HttpClient4Closure(final HttpClient client) {
		client_ = client;
	}
	
	@Override
	public final Either<F,S> doit(final HttpRequestBase request,
		final HttpContext context) {
		Either<F,S> result = null;
		// Any failures/exceptions encountered during request execution
		// (in a call to execute) are wrapped up as a Left() and are delt
		// with in the failure path below.
		final Either<HttpFailure,HttpSuccess> response = execute(request, context);
		try {
			if(response.success()) {
				result = Right.right(success(((Right<HttpFailure,HttpSuccess>)
					response).right_));
			} else {
				result = Left.left(failure(((Left<HttpFailure,HttpSuccess>)
					response).left_));
			}
		} catch (Exception e) {
			// Wrap up any failures/exceptions that might have occurred
			// while processing the response.
			result = Left.left(failure(new HttpFailure(e)));
		} finally {
			if(response.success()) {
				consumeQuietly(((Right<HttpFailure,HttpSuccess>)
					response).right_.getResponse());
			} else {
				consumeQuietly(((Left<HttpFailure,HttpSuccess>)
					response).left_.getResponse());
			}
		}
		return result;
	}
	
	private final Either<HttpFailure,HttpSuccess> execute(
		final HttpRequestBase request, final HttpContext context) {
		HttpResponse response = null;
		try {
			// Before the request is "executed" give the consumer an entry
			// point into the raw request object to tweak as necessary first.
			// Usually things like "signing" the request or modifying the
			// destination host are done here.
			before(request, context);
			// Actually execute the request, get a response.
			response = client_.execute(request, context);
			// Immediately after execution, only if the request was executed.
			after(response, context);
			// Check if the response was "successful".  The definition of
			// success is arbitrary based on what's defined in the check()
			// method.  The default success check is simply checking the
			// HTTP status code and if it's less than 400 (Bad Request) then
			// it's considered "good".  If the user wants evaluate this
			// response against some custom criteria, they should override
			// this check() method.
			if(check(response, context)) {
				return Right.right(new HttpSuccess(response, context));
			} else {
				return Left.left(new HttpFailure(response, context));
			}
		} catch (Exception e) {
			// Something went wrong with the request, abort it,
			// return failure.
			request.abort();
			return Left.left(new HttpFailure(e, response, context));
		}
	}
	
	/**
	 * Called only if the request is successful.  Success is defined by
	 * the boolean state that the {@link #check} method returns.  If
	 * {@link #check} returns true, the request is considered to be
	 * successful. If it returns false, the request failed.  
	 * @param success
	 * @return
	 * @throws Exception
	 */
	public abstract S success(final HttpSuccess success)
		throws Exception;
	
	/**
	 * Called only if the request is unsuccessful.  The default behavior,
	 * as implemented here, is to simply return null if the request failed.
	 * Consumers should override this default behavior if they need to extract
	 * more granular information about the failure, like an {@link Exception}
	 * or status code.
	 * @param failure
	 * @return null by default, override this if you want to return something else
	 */
	public F failure(final HttpFailure failure) {
		return null; // Default, return null on failure.
	}
	
}
