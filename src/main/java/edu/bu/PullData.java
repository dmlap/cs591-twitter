package edu.bu;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;


/**
 * Pulls XML data from twitter and stores it in the filesystem.
 * 
 */
public class PullData {
	private static final String TWITTER_API_URL = "http://api.twitter.com";
	private static final String TWITTER_STATUSES = TWITTER_API_URL + "/1/statuses/";
	private static final String USER_TIMELINE_XML = TWITTER_STATUSES + "user_timeline.xml";
	private static final String SCREEN_NAME_PARAM = "screen_name";
	private static final String PAGE_PARAM = "page";
	private static final String QPARAM_START = "?";
	private static final String PARAM_ASSIGNMENT = "=";
	private static final String PARAM_SEPARATOR = "&";
	private static final int PAGE_COUNT = 100;
	private final String username;
	private final int rounds;
	
	public PullData(String username, int rounds) {
		this.username = username;
		this.rounds = rounds;
	}
	
	private String apply(String base, String screenName, int page) {
		return new StringBuilder(base)
			.append(QPARAM_START)
			.append(SCREEN_NAME_PARAM)
			.append(PARAM_ASSIGNMENT)
			.append(screenName)
			.append(PARAM_SEPARATOR)
			.append(PAGE_PARAM)
			.append(PARAM_ASSIGNMENT)
			.append(page)
			.toString();
	}
	
	public void pull(final OutputStream outstream) throws ClientProtocolException, IOException {
		HttpClient httpClient = new DefaultHttpClient();
		for (int i = 0; i < PAGE_COUNT; ++i) {
			DataAccess.writeByteArray(httpClient.execute(new HttpGet(apply(USER_TIMELINE_XML,
					username, i)), new ResponseHandler<byte[]>() {
						@Override
						public byte[] handleResponse(HttpResponse response)
								throws ClientProtocolException, IOException {
							response.getEntity().writeTo(outstream);
							return EntityUtils.toByteArray(response.getEntity());
						}
					}), "output");
		}
	}
	

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
	    new PullData("dlapalomento", 1).pull(new FileOutputStream(new File("target", "output")));
	}
}
