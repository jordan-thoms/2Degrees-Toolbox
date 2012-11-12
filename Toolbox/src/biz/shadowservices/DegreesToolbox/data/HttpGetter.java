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
