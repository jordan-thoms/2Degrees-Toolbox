package biz.shadowservices.DegreesToolbox;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

public class HttpClientSingleton {
	private static HttpClient instance = null;
	private HttpClientSingleton() { }
	private static int TIMEOUT = 5002;
	public static HttpClient getInstance() {
		if (instance == null) {
			instance = new DefaultHttpClient();
			HttpParams httpParams = instance.getParams();
			HttpConnectionParams.setConnectionTimeout(httpParams, TIMEOUT);
			HttpConnectionParams.setSoTimeout(httpParams, TIMEOUT);
		}
		return instance;
	}
}
