/*******************************************************************************
 * Copyright (c) 2011 Jordan Thoms.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package biz.shadowservices.DegreesToolbox.data;

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
