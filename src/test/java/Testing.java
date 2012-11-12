import static com.kolich.common.DefaultCharacterEncoding.UTF_8;
import static com.kolich.http.KolichDefaultHttpClient.KolichHttpClientFactory.getNewInstanceWithProxySelector;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
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
				return EntityUtils.toString(success.getResponse().getEntity(), UTF_8);
			}
			@Override
			public Integer failure(final HttpFailure failure) {
				return failure.getResponse().getStatusLine().getStatusCode();
			}
		}.get("http://google.com");		
		if(result.success()) {
			System.out.println(result.right());
		} else {
			System.out.println(result.left());
		}
		
		final HttpResponseEither<Void,String> sResult =
			new HttpClientClosureExpectString(client)
				.get("http://mark.koli.ch");
		if(sResult.success()) {
			System.out.println(sResult.right());
		} else {
			System.out.println(sResult.left());
		}
		
		final HttpResponseEither<Exception,String> eResult = new HttpClientClosure<Exception,String>(client) {
			@Override
			public String success(final HttpSuccess success) throws Exception {
				return EntityUtils.toString(success.getResponse().getEntity(), UTF_8);
			}
			@Override
			public Exception failure(final HttpFailure failure) {
				return failure.getCause();
			}
		}.put("http://lskdjflksdfjslkf.vmwaresdfsdf.com");
		if(!eResult.success()) {
			System.out.println(eResult.left());
		}
		
		// Custom check for "success".
		final HttpResponseEither<Exception,String> cResult = new HttpClientClosure<Exception,String>(client) {
			@Override
			public boolean check(final HttpResponse response) {
				return (response.getStatusLine().getStatusCode() == 405);
			}
			@Override
			public String success(final HttpSuccess success) throws Exception {
				return EntityUtils.toString(success.getResponse().getEntity(), UTF_8);
			}
		}.put("http://google.com");
		if(cResult.success()) {
			System.out.println(eResult.right());
		}
		
		final HttpResponseEither<Exception,OutputStream> bResult = new HttpClientClosure<Exception,OutputStream>(client) {
			@Override
			public OutputStream success(final HttpSuccess success) throws Exception {
				final OutputStream os = new ByteArrayOutputStream();
				IOUtils.copy(success.getResponse().getEntity().getContent(), os);
				return os;
			}
			@Override
			public Exception failure(final HttpFailure failure) {
				return failure.getCause();
			}
		}.get("http://google.com");
		if(bResult.success()) {
			System.out.println("Loaded bytes into output stream!");
		}
		
		final OutputStream os = new ByteArrayOutputStream();
		final HttpResponseEither<Exception,Integer> stResult = new HttpClientClosure<Exception,Integer>(client) {
			@Override
			public Integer success(final HttpSuccess success) throws Exception {
				return IOUtils.copy(success.getResponse().getEntity().getContent(), os);
			}
			/*
			@Override
			public Exception failure(final HttpFailure failure) {
				return failure.getCause();
			}
			*/
		}.get("http://kolich.mobi");
		if(stResult.success()) {
			System.out.println("Loaded " + stResult.right() + " bytes.");
		}
				
	}
	
	public static class HttpClientClosureExpectString extends HttpClientClosure<Void,String> {
		public HttpClientClosureExpectString(final HttpClient client) {
			super(client);
		}
		@Override
		public String success(final HttpSuccess success) throws Exception {
			return EntityUtils.toString(success.getResponse().getEntity(), UTF_8);
		}
	}
	
	public static class HttpClientClosureExpectBytes extends HttpClientClosure<Void,byte[]> {
		public HttpClientClosureExpectBytes(final HttpClient client) {
			super(client);
		}
		@Override
		public byte[] success(final HttpSuccess success) throws Exception {
			return EntityUtils.toByteArray(success.getResponse().getEntity());
		}
	}
	
}
