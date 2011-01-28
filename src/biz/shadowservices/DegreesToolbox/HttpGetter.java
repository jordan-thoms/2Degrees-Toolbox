package biz.shadowservices.DegreesToolbox;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;

import android.util.Log;

public class HttpGetter {
	private static String TAG = "HttpGetter";
	private String response = null;
	private HttpGet httpget = null;
	public HttpGetter(URI uri) {
		httpget = new HttpGet(uri);
	}
	public HttpGetter(String uri) {
		httpget = new HttpGet(uri);
	}
	public String execute() throws ClientProtocolException, IOException  {
		//TODO: Better exception handling/retries here
		if (response == null) {
			HttpClient httpClient = HttpClientSingleton.getInstance();
			HttpResponse serverresponse = null;
			serverresponse = httpClient.execute(httpget);
			HttpEntity entity = serverresponse.getEntity();
			StringWriter writer = new StringWriter();
			IOUtils.copy(entity.getContent(), writer);
			response = writer.toString();
		}
		return response;
	}
}
