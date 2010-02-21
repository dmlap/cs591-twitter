package edu.bu;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Random;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import twitter4j.IDs;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;


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
	private static final int PAGE_COUNT = 1;
	private static final float SAMPLE_PCT = 0.50f;
	private static final int MAX_FOLLOWERS = 50;
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
	    //new PullData("dlapalomento", 1).pull(new FileOutputStream(new File("target", "output")));
		sampleUsers();
	}
	
	public static void sampleUsers() {
		try {
			TwitterFactory factory = new TwitterFactory();
	    	Twitter twitter = factory.getInstance();
	    	LinkedHashSet<Long> users = new LinkedHashSet<Long>();
	    	LinkedList<Long> queue = new LinkedList<Long>();
	    	Random rand = new Random();
	    	
	    	// Pull a random user from the public timeline
			long starter = getRandomUser();
	    	users.add(starter);
	    	queue.push(starter);		
			
			while(users.size() < 10) {
				if (queue.isEmpty())
					queue.push(getRandomUser());
				else {
					long user = queue.poll();
					IDs followers = twitter.getFollowersIDs(user);
					int numToPull = (int)Math.floor(followers.getIDs().length * SAMPLE_PCT);
					
					// Limit followers pull
					if (numToPull > MAX_FOLLOWERS)
						numToPull = MAX_FOLLOWERS;
		
					// Work through followers
					for(int i = 0; i < numToPull; i++) {
						user = (long)followers.getIDs()[rand.nextInt(followers.getIDs().length)];
						users.add(user);
						queue.push(user);
					}
				}
			}
			
			// Write users to file
			writeFile(users, "C:\\test.txt");
		} catch(TwitterException tex) {
			tex.printStackTrace();
		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public static long getRandomUser() throws TwitterException {
		TwitterFactory factory = new TwitterFactory();
    	Twitter twitter = factory.getInstance();
    	Random rand = new Random();
    	
    	ResponseList<Status> publicTimeline = twitter.getPublicTimeline();
		Status start = publicTimeline.get(rand.nextInt(publicTimeline.size()));
		
		return start.getId();
	}
	
	public static void writeFile(Object obj, String file) {
		ObjectOutputStream outputStream = null;
        
        try {
            outputStream = new ObjectOutputStream(new FileOutputStream(file));
            outputStream.writeObject(obj);
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            //Close the ObjectOutputStream
            try {
                if (outputStream != null) {
                    outputStream.flush();
                    outputStream.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
	}
}
