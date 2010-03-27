/**
 * 
 */
package edu.bu;

/**
 * A {@link Sensor} for Twitter-based {@link Incident}s.
 * 
 * @author dml
 * 
 */
public class TwitterUser implements Sensor {
	private final String username;

	public TwitterUser(String username) {
		this.username = username;
	}

	/* (non-Javadoc)
	 * @see edu.bu.Sensor#getId()
	 */
	@Override
	public String getId() {
		return username;
	}

	@Override
	public String toString() {
		return "{user: " + username + "}";
	}

}
