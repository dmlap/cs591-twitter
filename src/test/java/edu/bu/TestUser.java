/**
 * 
 */
package edu.bu;

/**
 * A {@link Sensor} for test {@link Incident}s.
 * 
 * @author dml
 * 
 */
public class TestUser implements Sensor<String> {
	private final String username;

	public TestUser(String username) {
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
