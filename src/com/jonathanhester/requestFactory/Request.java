package com.jonathanhester.requestFactory;

import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Map;

import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import android.os.AsyncTask;

public class Request<T> extends AsyncTask<Void, Void, Void> {

	private String path;
	private String method;
	private Map<String, String> urlParams;
	private RequestProxyObject proxy;
	private Receiver<String> receiver;
	private String error;
	private String response;

	public Request(String path, String method, Map<String, String> urlParams) {
		this.path = path;
		this.method = method;
		this.urlParams = urlParams;
	}
	
	private String getParams() {
		String encodedParamString = "";
		Iterator it = urlParams.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pairs = (Map.Entry) it.next();
			encodedParamString += pairs.getKey() + "="
					+ URLEncoder.encode((String) pairs.getValue()) + "&";
			it.remove(); // avoids a ConcurrentModificationException
		}
		return encodedParamString;
	}

	private String getPath() {
		if (urlParams == null)
			return path;
		return path + "?" + getParams();
	}

	public void fire(Receiver<String> receiver) {
		this.receiver = receiver;
		execute(null);
	}

	protected Void doInBackground(Void... params) {
		try {
			DefaultHttpClient httpclient = new DefaultHttpClient();
			HttpRequestBase httpMethod;
			if (method == "GET") {
				httpMethod = new HttpGet(getPath());
			} else if (method == "POST") {
				httpMethod = new HttpPost(getPath());
			} else if (method == "DELETE") {
				httpMethod = new HttpDelete(getPath());
			} else {
				httpMethod = new HttpGet(getPath());
			}
			if (proxy != null) {
				try {
					JSONObject data = proxy.getParams();
					StringEntity se = new StringEntity(data.toString());
					((HttpPost) httpMethod).setEntity(se);
				} catch (Exception e) {

				}
			}

			httpMethod.setHeader("Accept", "application/json");
			httpMethod.setHeader("Content-type", "application/json");

			ResponseHandler responseHandler = new BasicResponseHandler();
			response = httpclient.execute(httpMethod, responseHandler);
		} catch (Exception e) {
			this.error = e.getMessage();
		}

		return null;
	}

	protected void onPostExecute(Void unused) {
		if (this.error != null)
			receiver.onFailure(new ServerFailure(this.error));
		else
			receiver.onSuccess(response);
	}

}
