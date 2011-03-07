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

public class HttpGetter {
	// This class handles the simple getting of a url resource and returning the result
	// Since it's a pain to write out all the IOUtils stuff all the time.
	private static String TAG = "2DegreesHttpGetter";
	private String response = null;
	private HttpGet httpget = null;
	public HttpGetter(URI uri) {
		httpget = new HttpGet(uri);
	}
	public HttpGetter(String uri) {
		httpget = new HttpGet(uri);
	}
	public String execute() throws ClientProtocolException, IOException  {
		//TODO: Better exception handling/retries here, although just bumping it up seems to work
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
