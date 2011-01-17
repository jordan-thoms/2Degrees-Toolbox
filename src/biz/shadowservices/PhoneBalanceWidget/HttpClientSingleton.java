package biz.shadowservices.PhoneBalanceWidget;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;

public class HttpClientSingleton {
	private static HttpClient instance = null;
	private HttpClientSingleton() { }
	
	public static HttpClient getInstance() {
		if (instance == null) {
			instance = new DefaultHttpClient();
		}
		return instance;
	}
}
