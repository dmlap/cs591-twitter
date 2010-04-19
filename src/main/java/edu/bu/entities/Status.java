package edu.bu.entities;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.CascadeType;

import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

@Entity
public class Status {
	private Long id;
	private User user;
	private String status;
	private DateTime statusdate;
	private boolean processed;
	
	private Status() {}
	
	public static Status createStatus(Long id, User user, String status, DateTime statusdate, boolean processed) {
		Status result = new Status();
		result.id = id;
		result.user = user;
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
	
	@ManyToOne(cascade = CascadeType.REMOVE)
	public User getUser() {
		return user;
	}

	@SuppressWarnings("unused")
	private void setUser(User user) {
		this.user = user;
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
	
	public void setProcessed(boolean processed) {
		this.processed = processed;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + (processed ? 1231 : 1237);
		result = prime * result + ((status == null) ? 0 : status.hashCode());
		result = prime * result
				+ ((statusdate == null) ? 0 : statusdate.hashCode());
		result = prime * result + ((user == null) ? 0 : user.hashCode());
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
		Status other = (Status) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (processed != other.processed)
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
		if (user == null) {
			if (other.user != null)
				return false;
		} else if (!user.equals(other.user))
			return false;
		return true;
	}
}