import static com.kolich.common.DefaultCharacterEncoding.UTF_8;
import static com.kolich.http.KolichDefaultHttpClient.KolichHttpClientFactory.getNewInstanceNoProxySelector;

import org.apache.http.client.HttpClient;
import org.apache.http.util.EntityUtils;

import com.kolich.http.closure.HttpClientClosure;

public class Testing {
	
	public static void main(String[] args) {
		
		final HttpClient client = getNewInstanceNoProxySelector("foobar");
		
		/*
		final String result = new HttpClientClosure<String>(client) {
			@Override
			public String success(final HttpResponse response) throws Exception {
				return EntityUtils.toString(response.getEntity(), UTF_8);
			}
		}.get("http://google.com");
		*/
		
		final String sResult = new HttpClientClosureString(client).post("http://localhost:8080/api/");
		final String pResult = new HttpClientClosureString(client) {
			@Override
			public String success(final HttpSuccess success) throws Exception {
				System.out.println(success.status_);
				return super.success(success);
			}
		}.put("http://localhost:8080/api");
		
		//final byte[] bResult = new HttpClientClosureBytes(client).get("http://temp.koli.ch");
		
		System.out.println(sResult);
		System.out.println(pResult);
		
		//System.out.println(Arrays.toString(bResult));
		
	}
	
	public static class HttpClientClosureString extends HttpClientClosure<String> {
		public HttpClientClosureString(final HttpClient client) {
			super(client);
		}
		@Override
		public String success(final HttpSuccess success) throws Exception {
			return EntityUtils.toString(success.response_.getEntity(), UTF_8);
		}
	}
	
	public static class HttpClientClosureBytes extends HttpClientClosure<byte[]> {
		public HttpClientClosureBytes(final HttpClient client) {
			super(client);
		}
		@Override
		public byte[] success(final HttpSuccess success) throws Exception {
			return EntityUtils.toByteArray(success.response_.getEntity());
		}
	}

}
