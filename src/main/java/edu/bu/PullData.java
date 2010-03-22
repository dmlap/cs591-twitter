package edu.bu;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Random;
import java.util.Set;

import org.dom4j.*;
import org.dom4j.io.SAXReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import edu.bu.entities.Users;

/**
 * Pulls XML data from twitter and stores it in the filesystem.
 * 
 */
public class PullData {
	private static final String TWITTER_API_URL = "http://api.twitter.com";
	private static final String TWITTER_STATUSES = TWITTER_API_URL + "/1/statuses/";
	private static final String USER_TIMELINE_XML = TWITTER_STATUSES + "user_timeline.xml";
	private static final String PUBLIC_TIMELINE_XML = TWITTER_STATUSES + "public_timeline.xml";
	private static final String TWITTER_FOLLOWERS = TWITTER_API_URL + "/1/followers/";
	private static final String USER_FOLLOWER_IDS_XML = TWITTER_FOLLOWERS + "ids.xml";
	private static final String TWITTER_USERS = TWITTER_API_URL + "/1/users/";
	private static final String SHOW_XML = TWITTER_USERS + "show.xml";
	private static final String SCREEN_NAME_PARAM = "screen_name";
	private static final String USER_ID_PARAM = "user_id";
	private static final String PAGE_PARAM = "page";
	private static final String CURSOR_PARAM = "cursor";
	private static final String QPARAM_START = "?";
	private static final String PARAM_ASSIGNMENT = "=";
	private static final String PARAM_SEPARATOR = "&";
	private static final int PAGE_COUNT = 1;
	private static final float SAMPLE_PCT = 0.20f;
	private static final int MAX_SAMPLES = 50;
	private static final int MAX_SAMPLED_USERS = 10;
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
	
	private String apply(String base, Long userID, int cursor) {
		return new StringBuilder(base)
			.append(QPARAM_START)
			.append(USER_ID_PARAM)
			.append(PARAM_ASSIGNMENT)
			.append(userID)
			.append(PARAM_SEPARATOR)
			.append(CURSOR_PARAM)
			.append(PARAM_ASSIGNMENT)
			.append(cursor)
			.toString();
	}
	
	private String apply(String base, String userIDs) {
		return new StringBuilder(base)
			.append(QPARAM_START)
			.append(USER_ID_PARAM)
			.append(PARAM_ASSIGNMENT)
			.append(userIDs)
			.toString();
	}
	
	public void pull(final OutputStream outstream) throws ClientProtocolException, IOException {
		HttpClient httpClient = new DefaultHttpClient();
		for (int i = 0; i < PAGE_COUNT; ++i) {
			DataAccess.writeByteArray(httpClient.execute(new HttpGet(apply(
					USER_TIMELINE_XML, username, i)),
					new ResponseHandler<byte[]>() {
						@Override
						public byte[] handleResponse(HttpResponse response)
								throws ClientProtocolException, IOException {
							response.getEntity().writeTo(outstream);
							return EntityUtils
									.toByteArray(response.getEntity());
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
		
		//new PullData("dlapalomento", 1).sampleUsers();
		
		//Users user = new PullData("dlapalomento", 1).getRandomUser();
		//System.out.println(user.getName());
		//System.out.println(user.getId());
		//System.out.println(user.getDegree());
		
		/*Set<Users> users = new PullData("dlapalomento", 1).sampleFollowers(new Long(18936866));
		
		Iterator<Users> it = users.iterator();
		while (it.hasNext()) {
			Users user = it.next();
			System.out.println(user.getName());
			System.out.println(user.getId());
			System.out.println(user.getDegree());
		}*/
		
		new PullData("dlapalomento", 1).sampleUsers();
	}
	
	public void sampleUsers() throws ClientProtocolException, IOException, DocumentException {
		Set<Users> workingusers = new HashSet<Users>();
		Set<Users> users = new HashSet<Users>();
		
		// Get initial user
		Users user = null;
		
		while (user == null) {
			try {
				user = this.getRandomUser();
			} catch (Exception ex) {
				//ex.printStackTrace();
			}
		}
		
		workingusers.add(user);
		
		// Call recursive function
		Set<Users> sampleset = sample(workingusers, users);
		
		Iterator<Users> it = sampleset.iterator();
		while (it.hasNext()) {
			user = it.next();
			System.out.println(user.getName());
			System.out.println(user.getId());
			System.out.println(user.getDegree());
		}
	}
	
	public Set<Users> sample(Set<Users> workingset, Set<Users> users) throws ClientProtocolException, IOException, DocumentException {
		System.out.println("Recursive function");
		if (users.size() > MAX_SAMPLED_USERS)
			return users;
		else {
			// Sample followers for workingset
			if (workingset.size() == 0) {
				System.out.println("Working set is 0, get random user");
				// Get initial user
				Users user = null;
				
				while (user == null) {
					try {
						user = this.getRandomUser();
					} catch (Exception ex) {
						//ex.printStackTrace();
					}
				}
				System.out.println("Added random user, call recursively");
				workingset.add(user);
				users.add(user);
				
				return sample(workingset, users);
			} else {
				System.out.println("Iterate through working set");
				Set<Users> newworkingset = new HashSet<Users>();
				Iterator<Users> it = workingset.iterator();
				System.out.println("Iterate through users: " + String.valueOf(workingset.size()));
				int counter = 0;
				while (it.hasNext()) {
					System.out.println("User # " + String.valueOf(counter));
					Users user = it.next();
					users.add(user);
					newworkingset.addAll(this.sampleFollowers(user.getId()));
				}
				System.out.println("Call recursive");
				return sample(newworkingset, users);
			}
		}
	}
	
	/**
	 * 
	 * @return A random user from the public timeline
	 * @throws ClientProtocolException
	 * @throws IOException
	 * @throws DocumentException
	 */
	public Users getRandomUser() throws ClientProtocolException, IOException, DocumentException {
		HttpClient httpClient = new DefaultHttpClient();
		byte[] publictimeline = httpClient.execute(new HttpGet(PUBLIC_TIMELINE_XML),
				new ResponseHandler<byte[]>() {
					@Override
					public byte[] handleResponse(HttpResponse response)
							throws ClientProtocolException, IOException {
						return EntityUtils.toByteArray(response.getEntity());
					}
				});
		
		Set<Users> users = new HashSet<Users>();
		
		Long id = null;
		String name = "";
		int degree = -1;
		
		// Open the doc
		SAXReader reader = new SAXReader();
		Document document = reader.read(new ByteArrayInputStream(publictimeline));
		
		// Parse user id's
		Element root = document.getRootElement();
		for (Iterator<Element> itstatuses = root.elementIterator(); itstatuses.hasNext(); ) {
	 		Element statuses = itstatuses.next();
	 		for (Iterator<Element> itstatus = statuses.elementIterator(); itstatus.hasNext(); ){
	 			Element status = itstatus.next();
	 			if (status.getName().compareTo("user") == 0) {
	 				for (Iterator<Element> ituser = status.elementIterator(); ituser.hasNext(); ) {
	 					Element user = ituser.next();
	 					if (user.getName().compareTo("id") == 0) {
	 						id = Long.valueOf(user.getText());
	 					} else if (user.getName().compareTo("name") == 0) {
	 						name = user.getText();
	 					} else if (user.getName().compareTo("followers_count") == 0) {
	 						degree = Integer.parseInt(user.getText());
	 						
	 						Users pubuser = new Users();
	 						pubuser.createUser(id, name, degree);
	 						users.add(pubuser);

	 						id = null;
	 						name = "";
	 						degree = -1;
	 						
	 						break;
	 					}
	 				}
	 				break;
	 			}
	 		}
		}
		
		// Convert the set to an array
		Users[] ids = users.toArray(new Users[users.size()]);
		Random rand = new Random();
		return ids[rand.nextInt(ids.length)];
	}
	
	/**
	 * 
	 * @param idval The ID of the user to sample followers from
	 * @return The set of sampled users
	 * @throws ClientProtocolException
	 * @throws IOException
	 * @throws DocumentException
	 */
	public Set<Users> sampleFollowers(Long idval) throws ClientProtocolException, IOException, DocumentException {
		Set<Users> users = new HashSet<Users>();
		Set<String> userids = new HashSet<String>();
		System.out.println("Pull followers");
		HttpClient httpClient = new DefaultHttpClient();
		byte[] followers = httpClient.execute(new HttpGet(apply(USER_FOLLOWER_IDS_XML, idval, -1)),
				new ResponseHandler<byte[]>() {
					@Override
					public byte[] handleResponse(HttpResponse response)
							throws ClientProtocolException, IOException {
						return EntityUtils.toByteArray(response.getEntity());
					}
				});
		
		// Open the doc
		SAXReader reader = new SAXReader();
		Document document = reader.read(new ByteArrayInputStream(followers));
		System.out.println("Parse XML");
		// Parse user id's
		Element root = document.getRootElement();
		for (Iterator<Element> itidlist = root.elementIterator(); itidlist.hasNext(); ) {
	 		Element ids = itidlist.next();
	 		for (Iterator<Element> itids = ids.elementIterator(); itids.hasNext(); ){
	 			Element id = itids.next();
	 			userids.add(id.getText());
	 		}
		}
		
		// Sample users
		Set<String> samples = new HashSet<String>();
		String[] idnums = userids.toArray(new String[userids.size()]);
		int samplenum = ((int)Math.floor(idnums.length * SAMPLE_PCT) > MAX_SAMPLES) ? MAX_SAMPLES : (int)Math.floor(idnums.length * SAMPLE_PCT);
		System.out.println("Sample users");
		while (samples.size() < samplenum) {
			Random rand = new Random();
			samples.add(idnums[rand.nextInt(idnums.length)]);
		}
		
		// Get users info
		Iterator<String> it = samples.iterator();
		while (it.hasNext()) {
			System.out.println("Pull follower data");
			byte[] userdata = httpClient.execute(new HttpGet(apply(SHOW_XML, it.next())),
					new ResponseHandler<byte[]>() {
						@Override
						public byte[] handleResponse(HttpResponse response)
								throws ClientProtocolException, IOException {
							return EntityUtils.toByteArray(response.getEntity());
						}
					});
			
			Long id = null;
			String name = "";
			int degree = -1;
			
			// Open the doc
			reader = new SAXReader();
			document = reader.read(new ByteArrayInputStream(userdata));
			System.out.println("Parse follower data");
			// Parse user id's
			root = document.getRootElement();
			for (Iterator<Element> ituser = root.elementIterator(); ituser.hasNext(); ) {
		 		Element user = ituser.next();
		 		
		 		if (user.getName().compareTo("id") == 0) {
		 			id = Long.valueOf(user.getText());
		 		} else if (user.getName().compareTo("name") == 0) {
		 			name = user.getText();
		 		} else if (user.getName().compareTo("followers_count") == 0) {
		 			degree = Integer.parseInt(user.getText());
		 						
		 			Users follower = new Users();
		 			follower.createUser(id, name, degree);
		 			users.add(follower);

	 				id = null;
	 				name = "";
	 				degree = -1;
		 						
	 				break;
	 			}
	 		}
		}
		
		return users;
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
