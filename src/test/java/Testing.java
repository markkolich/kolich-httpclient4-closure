import static com.kolich.common.DefaultCharacterEncoding.UTF_8;
import static com.kolich.http.KolichDefaultHttpClient.KolichHttpClientFactory.getNewInstanceWithProxySelector;

import org.apache.http.client.HttpClient;
import org.apache.http.util.EntityUtils;

import com.kolich.http.closure.HttpClientClosure;
import com.kolich.http.closure.HttpClientClosure.HttpResponseEither;

public class Testing {
	
	public static void main(String[] args) {
		
		final HttpClient client = getNewInstanceWithProxySelector("foobar");
		
		final HttpResponseEither<Integer,String> result = new HttpClientClosure<Integer,String>(client) {
			@Override
			public String success(final HttpSuccess success) throws Exception {
				return EntityUtils.toString(success.response_.getEntity(), UTF_8);
			}
			@Override
			public Integer failure(final HttpFailure failure) {
				return failure.status_;
			}
		}.put("http://google.com");
		
		if(result.success()) {
			System.out.println(result.right());
		} else {
			System.out.println(result.left());
		}
		
		final HttpResponseEither<Void,String> sResult =
			new HttpClientClosureExpectString(client)
				.delete("http://mark.koli.ch");
		if(sResult.success()) {
			System.out.println(sResult.right());
		} else {
			System.out.println(sResult.left());
		}
		
		final HttpResponseEither<Exception,String> eResult = new HttpClientClosure<Exception,String>(client) {
			@Override
			public String success(final HttpSuccess success) throws Exception {
				return EntityUtils.toString(success.response_.getEntity(), UTF_8);
			}
			@Override
			public Exception failure(final HttpFailure failure) {
				return failure.cause_;
			}
		}.put("http://lskdjflksdfjslkf.vmwaresdfsdf.com");
		if(!eResult.success()) {
			System.out.println(eResult.left());
		}
		
		/*
		final String pResult = new HttpClientClosureExpectString(client) {
			@Override
			public String success(final HttpSuccess success) throws Exception {
				System.out.println(success.status_);
				return super.success(success);
			}
		}.get("http://google.com");
		*/
		
		//final byte[] bResult = new HttpClientClosureBytes(client).get("http://temp.koli.ch");
		
		
		//System.out.println(pResult);
		
		//System.out.println(Arrays.toString(bResult));
		
	}
	
	public static class HttpClientClosureExpectString extends HttpClientClosure<Void,String> {
		public HttpClientClosureExpectString(final HttpClient client) {
			super(client);
		}
		@Override
		public String success(final HttpSuccess success) throws Exception {
			return EntityUtils.toString(success.response_.getEntity(), UTF_8);
		}
	}
	
	public static class HttpClientClosureExpectBytes extends HttpClientClosure<Void,byte[]> {
		public HttpClientClosureExpectBytes(final HttpClient client) {
			super(client);
		}
		@Override
		public byte[] success(final HttpSuccess success) throws Exception {
			return EntityUtils.toByteArray(success.response_.getEntity());
		}
	}
	
}
