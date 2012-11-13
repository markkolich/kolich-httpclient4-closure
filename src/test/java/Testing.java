import static com.kolich.common.DefaultCharacterEncoding.UTF_8;
import static com.kolich.http.KolichDefaultHttpClient.KolichHttpClientFactory.getNewInstanceWithProxySelector;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import com.kolich.http.HttpClient4Closure;
import com.kolich.http.HttpClient4Closure.HttpResponseEither;

public class Testing {
	
	public static void main(String[] args) {
		
		final HttpClient client = getNewInstanceWithProxySelector("foobar");
		
		final HttpResponseEither<Integer,String> result = new HttpClient4Closure<Integer,String>(client) {
			@Override
			public void before(final HttpRequestBase request) {
				request.addHeader("Authorization", "super-secret-password");
			}
			@Override
			public String success(final HttpSuccess success) throws Exception {
				return EntityUtils.toString(success.getResponse().getEntity(), UTF_8);
			}
			@Override
			public Integer failure(final HttpFailure failure, final HttpContext context) {
				return failure.getStatusCode();
			}
		}.get("http://google.com");
		if(result.success()) {
			System.out.println(result.right());
		} else {
			System.out.println(result.left());
		}
		
		final HttpResponseEither<Void,Header[]> hResult = new HttpClient4Closure<Void,Header[]>(client) {
			@Override
			public Header[] success(final HttpSuccess success) throws Exception {
				return success.getResponse().getAllHeaders();
			}
		}.head("http://example.com");
		if(hResult.success()) {
			System.out.println("Fetched " + hResult.right().length + " request headers.");
		}
		
		final HttpResponseEither<Void,String> sResult =
			new HttpClientClosureExpectString(client)
				.get("http://mark.koli.ch");
		if(sResult.success()) {
			System.out.println(sResult.right());
		} else {
			System.out.println(sResult.left());
		}
		
		final HttpResponseEither<Exception,String> eResult = new HttpClient4Closure<Exception,String>(client) {
			@Override
			public String success(final HttpSuccess success) throws Exception {
				return EntityUtils.toString(success.getResponse().getEntity(), UTF_8);
			}
			@Override
			public Exception failure(final HttpFailure failure, final HttpContext context) {
				return failure.getCause();
			}
		}.put("http://lskdjflksdfjslkf.vmwaresdfsdf.com");
		if(!eResult.success()) {
			System.out.println(eResult.left());
		}
		
		// Custom check for "success".
		final HttpResponseEither<Exception,String> cResult = new HttpClient4Closure<Exception,String>(client) {
			@Override
			public boolean check(final HttpResponse response, final HttpContext context) {
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
		
		final HttpResponseEither<Exception,OutputStream> bResult = new HttpClient4Closure<Exception,OutputStream>(client) {
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
		final HttpResponseEither<Exception,Integer> stResult = new HttpClient4Closure<Exception,Integer>(client) {
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
	
	public static class HttpClientClosureExpectString extends HttpClient4Closure<Void,String> {
		public HttpClientClosureExpectString(final HttpClient client) {
			super(client);
		}
		@Override
		public String success(final HttpSuccess success) throws Exception {
			return EntityUtils.toString(success.getResponse().getEntity(), UTF_8);
		}
	}
	
	public static class HttpClientClosureExpectBytes extends HttpClient4Closure<Void,byte[]> {
		public HttpClientClosureExpectBytes(final HttpClient client) {
			super(client);
		}
		@Override
		public byte[] success(final HttpSuccess success) throws Exception {
			return EntityUtils.toByteArray(success.getResponse().getEntity());
		}
	}
	
}
