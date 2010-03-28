package edu.bu.entities;

import javax.persistence.Id;
import org.joda.time.DateTime;

public class Statuses {
	private Long id;
	private Long userID;
	private String status;
	private DateTime statusdate;
	
	public Statuses() {}
	
	public void createUser(Long id, Long userID, String status, DateTime statusdate) {
		this.id = id;
		this.userID = userID;
		this.status = status;
		this.statusdate = statusdate;
	}
	
	@Id
	public Long getId() {
		return this.id;
	}
	
	public Long getUserID() {
		return this.userID;
	}
	
	public String getStatus() {
		return this.status;
	}
	
	public DateTime getStatusDate() {
		return this.statusdate;
	}
}
