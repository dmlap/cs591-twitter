package edu.bu;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;

import org.dom4j.*;
import org.dom4j.io.SAXReader;
import org.h2.jdbc.JdbcBatchUpdateException;
import org.hibernate.exception.ConstraintViolationException;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatterBuilder;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import edu.bu.entities.Status;
import edu.bu.entities.UserDao;
import edu.bu.entities.User;

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
	private static final String SINCE_ID_PARAM = "since_id";
	private static final String COUNT_PARAM = "count";
	private static final String QPARAM_START = "?";
	private static final String PARAM_ASSIGNMENT = "=";
	private static final String PARAM_SEPARATOR = "&";
	private static final int PAGE_COUNT = 1;
	private static final float SAMPLE_PCT = 0.20f;
	private static final int MAX_SAMPLES = 50;
	private static final int MAX_SAMPLED_USERS = 100;
	private final String username;
	
	public PullData(String username) {
		this.username = username;
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
	
	private String apply(String base, Long userID, Long sinceID, int count) {
		StringBuilder retval = new StringBuilder(base)
			.append(QPARAM_START)
			.append(USER_ID_PARAM)
			.append(PARAM_ASSIGNMENT)
			.append(userID)
			.append(PARAM_SEPARATOR)
			.append(COUNT_PARAM)
			.append(PARAM_ASSIGNMENT)
			.append(count);
		
		if (sinceID > 0) {
		 retval.append(PARAM_SEPARATOR)
			.append(SINCE_ID_PARAM)
			.append(PARAM_ASSIGNMENT)
			.append(sinceID);
		}
		
		return retval.toString();
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
		
		new PullData("dlapalomento").sampleUsers();
	}
	
	public void sampleUsers() throws ClientProtocolException, IOException, DocumentException {
		Set<Long> workingusers = new HashSet<Long>();
		Set<User> users = new HashSet<User>();
		
		// Get initial user
		User user = null;
		System.out.println("Get random user");
		while (user == null) {
			try {
				user = this.getRandomUser();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		
		workingusers.add(user.getId());
		
		// Call recursive function
		Set<User> sampleset = sample(workingusers, users);
		
		UserDao dao = new UserDao();
		Iterator<User> it = sampleset.iterator();
		while (it.hasNext()) {
			user = it.next();
			dao.save(user);
			System.out.println(user.getName());
			System.out.println(user.getId());
			System.out.println(user.getDegree());
		}
	}
		
	public Set<User> sample(Set<Long> workingset, Set<User> users) throws ClientProtocolException, IOException, DocumentException {
		System.out.println("Recursive function");
		if (users.size() > MAX_SAMPLED_USERS)
			return users;
		else {
			// Sample followers for workingset
			if (workingset.size() == 0) {
				System.out.println("Working set is 0, get random user");
				// Get initial user
				User user = null;
				
				while (user == null) {
					try {
						user = this.getRandomUser();
					} catch (Exception ex) {
						//ex.printStackTrace();
					}
				}
				System.out.println("Added random user, call recursively");
				workingset.add(user.getId());
				
				return sample(workingset, users);
			} else {
				System.out.println("Iterate through working set");
				Set<Long> newworkingset = new HashSet<Long>();
				Iterator<Long> it = workingset.iterator();
				System.out.println("Iterate through users: " + String.valueOf(workingset.size()));
				int counter = 0;
				UserDao usrdao = new UserDao();
				while(it.hasNext()) {
					System.out.println("User # " + String.valueOf(counter));
					counter++;
					Long userid = it.next();
					
					try {
						User user = this.getUserData(userid);
						users.add(user);
						if (user.getId() != null) {
							usrdao.save(user);
							newworkingset.addAll(this.sampleFollowers(userid));
						}
					} catch (SocketTimeoutException ex) {
						System.out.println("Get user data timeout");
					} catch (SocketException ex) {
						System.out.println("Get user data timeout");
					} catch (ConstraintViolationException ex) {
						System.out.println("Duplicate user");
					}
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
	public User getRandomUser() throws ClientProtocolException, IOException, DocumentException {
		HttpClient httpClient = new DefaultHttpClient();

		byte[] publictimeline = httpClient.execute(new HttpGet(PUBLIC_TIMELINE_XML),
				new ResponseHandler<byte[]>() {
					@Override
					public byte[] handleResponse(HttpResponse response)
							throws ClientProtocolException, IOException {
						return EntityUtils.toByteArray(response.getEntity());
					}
				});
		
		Set<User> users = new HashSet<User>();
		
		Long id = null;
		String name = "";
		int degree = -1;

		// Open the doc
		SAXReader reader = new SAXReader();
		Document document = reader.read(new ByteArrayInputStream(publictimeline));

		// Parse user id's
		Element root = document.getRootElement();
		for (@SuppressWarnings("unchecked")Iterator<Element> itstatuses = root.elementIterator(); itstatuses.hasNext(); ) {
	 		Element statuses = itstatuses.next();
	 		for (@SuppressWarnings("unchecked")Iterator<Element> itstatus = statuses.elementIterator(); itstatus.hasNext(); ){
	 			Element status = itstatus.next();
	 			if (status.getName().compareTo("user") == 0) {
	 				for (@SuppressWarnings("unchecked")Iterator<Element> ituser = status.elementIterator(); ituser.hasNext(); ) {
	 					Element user = ituser.next();
	 					if (user.getName().compareTo("id") == 0) {
	 						id = Long.valueOf(user.getText());
	 					} else if (user.getName().compareTo("name") == 0) {
	 						name = user.getText();
	 					} else if (user.getName().compareTo("followers_count") == 0) {
	 						degree = Integer.parseInt(user.getText());
	 						
	 						User pubuser = User.createUser(id, name, degree);
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
		User[] ids = users.toArray(new User[users.size()]);
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
	public Set<Long> sampleFollowers(Long idval) throws ClientProtocolException, IOException, DocumentException {
		Set<Long> userids = new HashSet<Long>();

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
		
		// Parse user id's
		Element root = document.getRootElement();
		for (@SuppressWarnings("unchecked")Iterator<Element> itidlist = root.elementIterator(); itidlist.hasNext(); ) {
	 		Element ids = itidlist.next();
	 		for (@SuppressWarnings("unchecked")Iterator<Element> itids = ids.elementIterator(); itids.hasNext(); ){
	 			Element id = itids.next();
	 			userids.add(Long.parseLong(id.getText()));
	 		}
		}
		
		// Sample users
		Set<Long> samples = new HashSet<Long>();
		System.out.println("Followers pulled = " + userids.size());
		Long[] idnums = userids.toArray(new Long[userids.size()]);

		int samplenum = ((int)Math.floor(idnums.length * SAMPLE_PCT) > MAX_SAMPLES) ? MAX_SAMPLES : (int)Math.floor(idnums.length * SAMPLE_PCT);

		while (samples.size() < samplenum) {
			Random rand = new Random();
			samples.add(idnums[rand.nextInt(idnums.length)]);
		}
		System.out.println("Total samples " + samples.size());
		
		return samples;
	}
	
	public User getUserData(Long idval) throws ClientProtocolException, IOException, DocumentException {
		// Get users info
		System.out.println("Pull follower data");
		User follower = User.createUser(null, "", -1);
		
		HttpClient httpClient = new DefaultHttpClient();
		HttpGet httpget = new HttpGet(apply(SHOW_XML, idval.toString()));
		httpget.getParams().setParameter("http.socket.timeout", new Integer(10000));
		byte[] userdata = httpClient.execute(httpget,
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
		SAXReader reader = new SAXReader();
		Document document = reader.read(new ByteArrayInputStream(userdata));
		System.out.println("Parse follower data");
		// Parse user id's
		Element root = document.getRootElement();
		for (@SuppressWarnings("unchecked")Iterator<Element> ituser = root.elementIterator(); ituser.hasNext(); ) {
	 		Element user = ituser.next();
	 		
	 		if (user.getName().compareTo("id") == 0) {
	 			id = Long.valueOf(user.getText());
	 		} else if (user.getName().compareTo("name") == 0) {
	 			name = user.getText();
	 		} else if (user.getName().compareTo("followers_count") == 0) {
	 			degree = Integer.parseInt(user.getText());
	 			follower = User.createUser(id, name, degree);
	 			
 				id = null;
 				name = "";
 				degree = -1;
	 						
 				break;
 			}
 		}
		
		return follower;
	}
	
	public Set<Status> getUserStatuses(Long userID) throws ClientProtocolException, IOException {
		Set<Status> statuses = new HashSet<Status>();
		
		HttpClient httpClient = new DefaultHttpClient();
		byte[] status = httpClient.execute(new HttpGet(apply(USER_FOLLOWER_IDS_XML, userID, -1)),
				new ResponseHandler<byte[]>() {
					@Override
					public byte[] handleResponse(HttpResponse response)
							throws ClientProtocolException, IOException {
						return EntityUtils.toByteArray(response.getEntity());
					}
				});
		
		return statuses;
	}
	
	public DateTime parseUTCDate(String utcdate) {
		return new DateTimeFormatterBuilder()
		.appendDayOfWeekShortText()
		.appendLiteral(" ")
		.appendMonthOfYearShortText()
		.appendLiteral(" ")
		.appendDayOfMonth(2)
		.appendLiteral(" ")
		.appendHourOfDay(2)
		.appendLiteral(":")
		.appendMinuteOfHour(2)
		.appendLiteral(":")
		.appendSecondOfMinute(2)
		.appendLiteral(" ")
		.appendTimeZoneOffset(null, false, 2, 2)
		.appendLiteral(" ")
		.appendYear(4, 4)
		.toFormatter().parseDateTime(utcdate);
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
