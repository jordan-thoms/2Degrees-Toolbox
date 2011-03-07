package biz.shadowservices.DegreesToolbox;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.protocol.HTTP;

import android.util.Log;

public class HttpPoster {
	// This class handles the simple posting of a url resource and returning the result
	// Since it's a pain to write out all the IOUtils stuff all the time.
	private static String TAG = "2DegreesHttpGetter";
	private String response = null;
	private HttpPost httppost = null;
	public HttpPoster(URI uri, List<? extends NameValuePair> values)  {
		httppost = new HttpPost(uri);
		try {
			httppost.setEntity(new UrlEncodedFormEntity(values, HTTP.UTF_8));
		} catch (UnsupportedEncodingException e) {
			Log.e(TAG, "Error: Unsupported encoding exception on " + httppost.getURI());
		}
	}
	public HttpPoster(String uri, List<? extends NameValuePair> values)  {
		httppost = new HttpPost(uri);
		try {
			httppost.setEntity(new UrlEncodedFormEntity(values, HTTP.UTF_8));
		} catch (UnsupportedEncodingException e) {
			Log.e(TAG, "Error:  Unsupported encoding exception on " + httppost.getURI());
		}
	}
	public String execute() throws ClientProtocolException, IOException  {
		//TODO: Better exception handling/retries here
		if (response == null) {
			HttpClient httpClient = HttpClientSingleton.getInstance();
			HttpResponse serverresponse = null;
			serverresponse = httpClient.execute(httppost);
			HttpEntity entity = serverresponse.getEntity();
			StringWriter writer = new StringWriter();
			IOUtils.copy(entity.getContent(), writer);
			response = writer.toString();
		}
		return response;
	}
}
