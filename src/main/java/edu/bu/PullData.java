package edu.bu;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;
import java.util.Set;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.hibernate.exception.ConstraintViolationException;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatterBuilder;

import edu.bu.entities.*;

/**
 * Pulls XML data from twitter and stores it in a database.
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
	private static final String USER_ID_PARAM = "user_id";
	private static final String CURSOR_PARAM = "cursor";
	private static final String SINCE_ID_PARAM = "since_id";
	private static final String COUNT_PARAM = "count";
	private static final String QPARAM_START = "?";
	private static final String PARAM_ASSIGNMENT = "=";
	private static final String PARAM_SEPARATOR = "&";
	private static final float SAMPLE_PCT = 0.20f;
	private static final int MAX_SAMPLES = 50;
	private static final int MAX_SAMPLED_USERS = 100;
	private static final int MAX_USERS_FOR_STATUSES = 150; 	//150 Rate limit an hour
	private static final int MAX_STATUSES_TO_PULL = 200;  	//Twitter limit
	private static final int MAX_UNPROCESSED_USERS = 10;	//Process in chunks
	private static final int MAX_UNPROCESSED_HASHES = 15;	//Process in chunks since statuses come back 
	private static final int MAX_STARTERS = 5;
	
	public PullData() {	}
	
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
		
		if (sinceID != null) {
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
	
	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		//new PullData().sampleUsers();
		
		//new PullData().getStatuses();
		
		new PullData().processStatuses();
		
		/*List<Starter> starters = new PullData().getStarters();
		UserDao dao = new UserDao();
		ListIterator<Starter> it = starters.listIterator();
		while (it.hasNext()) {
			Starter starter = it.next();
			User user = dao.get(starter.getId());
			System.out.print("Starter: ");
			System.out.print(starter.getId());
			System.out.print(", Score: ");
			System.out.println(starter.getScore());
			System.out.print("Username: " + user.getName() + ", Degree: ");
			System.out.println(user.getDegree());
		}*/
		
		//new PullData().getStatistics();
	}
	
	/**
	 * Function to sample Users from Twitter API
	 * 
	 * @throws ClientProtocolException
	 * @throws IOException
	 * @throws DocumentException
	 */
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
	
	/**
	 * Recursive function to collect users
	 * 
	 * @param workingset 
	 * 			- The current working set to pull data for
	 * @param users
	 * 			- The current set of collected users
	 * @return A set of Users to add to the database
	 * @throws ClientProtocolException
	 * @throws IOException
	 * @throws DocumentException
	 */
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
	 * Randomly selects one of the users from the public timeline
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
		
		// Check for rate limit
		if (document.getStringValue().contains("Rate limit exceeded.")) {
			System.out.println("Rate limit exceeded");
			System.exit(0);
		}

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
	 * @param idval
	 * 			- The ID of the user to sample followers from
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
		
		// Check for rate limit
		if (document.getStringValue().contains("Rate limit exceeded.")) {
			System.out.println("Rate limit exceeded");
			System.exit(0);
		}
		
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
	
	/**
	 * 
	 * @param idval
	 * 			- The ID of the user to get
	 * @return A User object with all the user's information
	 * @throws ClientProtocolException
	 * @throws IOException
	 * @throws DocumentException
	 */
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
		
		// Check for rate limit
		if (document.getStringValue().contains("Rate limit exceeded.")) {
			System.out.println("Rate limit exceeded");
			System.exit(0);
		}
		
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
	
	/**
	 * Pulls statuses for a block of users and saves them
	 * 
	 * @throws ClientProtocolException
	 * @throws IOException
	 * @throws DocumentException
	 */
	public void getStatuses() throws ClientProtocolException, IOException, DocumentException {
		// Pull LastID
		LastIDDao lastIdDao = new LastIDDao();
		LastID lastid = lastIdDao.get("UserID");
		
		// If null create and save
		if (lastid == null) {
			lastid = LastID.createLastID(0L, "UserID");
			lastIdDao.save(lastid);
		}

		// Pull 150 users
		UserDao userDao = new UserDao();
		List<User> users = userDao.findWithIdGt(lastid.getId(), MAX_USERS_FOR_STATUSES);
		
		// Pull statuses
		StatusDao statusDao = new StatusDao();
		ListIterator<User> it = users.listIterator();
		while (it.hasNext()) {
			List<Status> statuses = getUserStatuses(it.next());
			if (statuses.size() > 0) {
				Status status = statuses.get(0);
				statuses.remove(0);
				
				if (statuses.size() > 1)
					statusDao.save(status, statuses.toArray(new Status[statuses.size()]));
				else
					statusDao.save(status);
				
				System.out.println(String.valueOf(statuses.size()) + " statuses saved");
			}
		}
		
		// Save last user ID
		lastIdDao.delete(lastid);
		User maxuser = userDao.findMaxId();
		if (maxuser.getId() != users.get(users.size()-1).getId()) {
			lastid = LastID.createLastID(users.get(users.size()-1).getId(), "UserID");
		} else {
			lastid = LastID.createLastID(0L, "UserID");
		}
		lastIdDao.save(lastid);
		
		System.out.print("Saved statuses for ");
		System.out.print(users.size());
		System.out.print(" users");
	}
	
	/**
	 * Pulls statuses for the specified user
	 * 
	 * @param user
	 * 			- The user to get data for
	 * @return A list of statuses
	 * @throws ClientProtocolException
	 * @throws IOException
	 * @throws DocumentException
	 */
	private List<Status> getUserStatuses(User user) throws ClientProtocolException, IOException, DocumentException {
		List<Status> statuses = new ArrayList<Status>();
		
		StatusDao dao = new StatusDao();
		System.out.println("Get max status for user " + user.getId().toString());
		Status maxstatus = dao.getMaxStatusForUser(user.getId());
		Long sinceID;
		
		if (maxstatus == null)
			sinceID = 0L;
		else
			sinceID = maxstatus.getId();
		
		HttpClient httpClient = new DefaultHttpClient();
		HttpGet httpget = new HttpGet(apply(USER_TIMELINE_XML, user.getId(), sinceID, MAX_STATUSES_TO_PULL));
		httpget.getParams().setParameter("http.socket.timeout", new Integer(10000));
		byte[] statusxml;
		
		try {
			statusxml = httpClient.execute(httpget,
					new ResponseHandler<byte[]>() {
						@Override
						public byte[] handleResponse(HttpResponse response)
							throws ClientProtocolException, IOException {
							return EntityUtils.toByteArray(response.getEntity());
						}
					});
		} catch (SocketTimeoutException ex) {
			System.err.println("Get user data timeout");
			return statuses;
		} catch (SocketException ex) {
			System.err.println("Get user data timeout");
			return statuses;
		}
			
		Long id = null;
		String statustxt = "";
		DateTime statusdate = null;
		
		// Open the doc
		SAXReader reader = new SAXReader();
		try {
			Document document = reader.read(new ByteArrayInputStream(statusxml));
			
			// Check for rate limit
			if (document.getStringValue().contains("Rate limit exceeded.")) {
				System.out.println("Rate limit exceeded");
				System.exit(0);
			}
			
			System.out.println("Parse status data");
			
			// Parse user id's
			Element root = document.getRootElement();
			for (@SuppressWarnings("unchecked")Iterator<Element> itstatus = root.elementIterator(); itstatus.hasNext(); ) {
		 		Element status = itstatus.next();
		 		for (@SuppressWarnings("unchecked")Iterator<Element> itdetail = status.elementIterator(); itdetail.hasNext(); ) {
		 			Element detail = itdetail.next();
	
			 		if (detail.getName().compareTo("created_at") == 0) {
			 			statusdate = this.parseUTCDate(detail.getText());
			 		} else if (detail.getName().compareTo("id") == 0) {
			 			id = Long.parseLong(detail.getText());
			 		} else if (detail.getName().compareTo("text") == 0) {
			 			statustxt = detail.getText();
			 		
			 			statuses.add(Status.createStatus(id, user, statustxt, statusdate, false));
			 			
		 				id = null;
		 				statustxt = "";
		 				statusdate = null;
			 						
		 				break;
		 			}
		 		}
	 		}
		} catch (Exception ex) {
			System.err.println("Error parsing");
			ex.printStackTrace();
		}
		
		return statuses;
	}
	
	/**
	 * Parses a UTC date into joda DateTime
	 * @param utcdate
	 * 			- A string containing the UTC formatted date
	 * @return A joda DateTime of the parsed date time
	 */
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

	/**
	 * Processes statuses parsing out hashes
	 * 
	 * @throws IOException
	 */
	public void processStatuses() throws IOException {
		// Pull unprocessed statuses
		StatusDao statusDao = new StatusDao();
		List<Status> unprocessed = statusDao.getUnprocessed(MAX_UNPROCESSED_USERS);
		Stemmer stemmer = new Stemmer();
		StatusDao statusdao = new StatusDao();
		
		while (unprocessed.size() > 0) {
			ListIterator<Status> it = unprocessed.listIterator();
			while (it.hasNext()) {
				Status status = it.next();
				
				// Parse status
				if (status.getStatus().contains("#")) {
					String[] words = status.getStatus().split("\\s+");
					for(int i = 0; i < words.length; i++) {
						String word = words[i];
						if (word.charAt(0) == '#') {
							// Get hash stem
							TokenStream stream = stemmer.tokenStream("hash", new StringReader(word));
							TermAttribute termAttrib = stream.addAttribute(TermAttribute.class);
							
							stream.reset();
							
							if (stream.incrementToken()) {
								String stem = termAttrib.term();
								HashDao dao = new HashDao();
								Hash hash = dao.get(stem);
								
								if (hash == null) {
									// Add new hash
									List<Status> statuses = new ArrayList<Status>();
									statuses.add(status);
									hash = Hash.createHash(stem, false, statuses);
									dao.save(hash);
								} else {
									// Update status list for hash
									hash.getStatuses().add(status);
									dao.update(hash);
								}
							}
							
							stream.end();
							stream.close();
						}
					}
				}
				
				// Mark the status as processed
				status.setProcessed(true);
				statusdao.update(status);
			}
			
			// Refill unprocessed
			unprocessed = statusDao.getUnprocessed(MAX_UNPROCESSED_USERS);
		}
	}
	
	/**
	 * Processes the hashes determining best starters
	 * 
	 * @return A list of the top starters
	 */
	public List<Starter> getStarters() {
		HashDao hashDao = new HashDao();
		StatusDao statusDao = new StatusDao();
		StarterDao starterDao = new StarterDao();
		
		// Clear starter table
		starterDao.deleteAll();
		
		List<Hash> unprocessed = hashDao.getUnprocessed(MAX_UNPROCESSED_HASHES);
		
		while (unprocessed.size() > 0) {
			ListIterator<Hash> it = unprocessed.listIterator();
			
			while (it.hasNext()) {
				Hash hash = it.next();
				List<Status> statuses = statusDao.getStartersForHash(hash.getHash(), MAX_STARTERS);
				ListIterator<Status> itstatuses = statuses.listIterator();
				int counter = 0;
				
				while (itstatuses.hasNext()) {
					Status status = itstatuses.next();
					Starter starter = starterDao.get(status.getUser().getId());
					
					if (starter == null) {
						starter = Starter.createStarter(status.getUser().getId(), statuses.size() - counter);
						starterDao.save(starter);
					} else {
						starter.setScore(starter.getScore() + statuses.size() - counter);
						starterDao.update(starter);
					}
					counter++;
				}
				
				// Update hash
				hash.setProcessed(true);
				hashDao.update(hash);
			}
			
			// Refill unprocessed
			unprocessed = hashDao.getUnprocessed(MAX_UNPROCESSED_HASHES);
		}
		
		// Reset hash processed
		this.resetHashes();
		
		return starterDao.getTopStarters(MAX_STARTERS);
	}
	
	/**
	 * Resets all of the hashes to not processed
	 */
	public void resetHashes() {
		HashDao dao = new HashDao();
		List<Hash> hashes = dao.getProcessed(MAX_UNPROCESSED_HASHES);
		
		while (hashes.size() > 0) {
			ListIterator<Hash> it = hashes.listIterator();
			
			while (it.hasNext()) {
				Hash hash = it.next();
				hash.setProcessed(false);
				dao.update(hash);
			}
			
			hashes = dao.getProcessed(MAX_UNPROCESSED_HASHES); 
		}
	}
	
	/**
	 * Prints out stats about the current dataset
	 */
	public void getStatistics() {
		System.out.println("***** Statistics *****");
		UserDao userDao = new UserDao();
		Long usercount = userDao.getCount();
		System.out.println("Total Users: " + usercount);
		
		StatusDao statusDao = new StatusDao();
		Long statuscount = statusDao.getCount();
		System.out.println("Total Statuses: " + statuscount);
		
		HashDao hashDao = new HashDao();
		Long hashcount = hashDao.getCount();
		System.out.println("Total Hashes: " + hashcount + "\n");
		
		System.out.println("Top 10 Users (degree)");
		List<User> users = userDao.topTen();
		ListIterator<User> ituser = users.listIterator();
		int counter = 1;
		while (ituser.hasNext()) {
			User user = ituser.next();
			System.out.println(counter + ". \tUserID: " + user.getId());
			System.out.println("\tName: " + user.getName());
			System.out.println("\tDegree: " + user.getDegree() + "\n");
			counter++;
		}
		
		System.out.println("Top 10 Hashes");
		List<Hash> hashes = hashDao.topTen();
		ListIterator<Hash> ithash = hashes.listIterator();
		counter = 1;
		while (ithash.hasNext()) {
			Hash hash = ithash.next();
			System.out.println(counter + ". \tHash: " + hash.getHash());
			System.out.println("\tCount: " + hash.getStatuses().size() + "\n");
			counter++;
		}
		
		System.out.println("Top 10 Starters");
		StarterDao starterDao = new StarterDao();
		List<Starter> starters = starterDao.getTopStarters(10);
		ListIterator<Starter> it = starters.listIterator();
		counter = 1;
		while (it.hasNext()) {
			Starter starter = it.next();
			User user = userDao.get(starter.getId());
			System.out.println(counter + "\tStarter: " + starter.getId() + ", Score: " + starter.getScore());
			System.out.println("\tUsername: " + user.getName() + ", Degree: " + user.getDegree() + "\n");
			counter++;
		}
	}
}
