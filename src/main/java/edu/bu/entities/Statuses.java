package edu.bu.entities;

import javax.persistence.Entity;
import javax.persistence.Id;

import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

@Entity
public class Statuses {
	private Long id;
	private Long userID;
	private String status;
	private DateTime statusdate;
	private boolean processed;
	
	private Statuses() {}
	
	public static Statuses createStatus(Long id, Long userID, String status, DateTime statusdate, boolean processed) {
		Statuses result = new Statuses();
		result.id = id;
		result.userID = userID;
		result.status = status;
		result.statusdate = statusdate;
		result.processed = processed;
		
		return result;
	}
	
	@Id
	public Long getId() {
		return this.id;
	}
	
	@SuppressWarnings("unused")
	private void setId(Long id) {
		this.id = id;
	}
	
	public Long getUserId() {
		return this.userID;
	}
	
	@SuppressWarnings("unused")
	private void setUserId(Long userID) {
		this.userID = userID;
	}
	
	public String getStatus() {
		return this.status;
	}
	
	@SuppressWarnings("unused")
	private void setStatus(String status) {
		this.status = status;
	}
	
	@Type(type="org.joda.time.contrib.hibernate.PersistentDateTime")
	public DateTime getStatusDate() {
		return this.statusdate;
	}
	
	@SuppressWarnings("unused")
	private void setStatusDate(DateTime statusdate) {
		this.statusdate = statusdate;
	}
	
	public boolean getProcessed() {
		return this.processed;
	}
	
	@SuppressWarnings("unused")
	private void setProcessed(boolean processed) {
		this.processed = processed;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((status == null) ? 0 : status.hashCode());
		result = prime * result
				+ ((statusdate == null) ? 0 : statusdate.hashCode());
		result = prime * result + ((userID == null) ? 0 : userID.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Statuses other = (Statuses) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (status == null) {
			if (other.status != null)
				return false;
		} else if (!status.equals(other.status))
			return false;
		if (statusdate == null) {
			if (other.statusdate != null)
				return false;
		} else if (!statusdate.equals(other.statusdate))
			return false;
		if (userID == null) {
			if (other.userID != null)
				return false;
		} else if (!userID.equals(other.userID))
			return false;
		return true;
	}
}