package edu.bu.entities;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;

@Entity
public class Hash {
	private String hash;
	private boolean processed;
	private List<Status> statuses;
	
	private Hash() {}
	
	public static Hash createHash(String hash, boolean processed, List<Status> statuses) {
		Hash result = new Hash();
		result.hash = hash;
		result.processed = processed;
		result.statuses = statuses;
		
		return result;
	}
	
	@Id
	public String getHash() {
		return this.hash;
	}
	
	@SuppressWarnings("unused")
	private void setHash(String hash) {
		this.hash = hash;
	}
	
	public boolean getProcessed() {
		return this.processed;
	}
	
	public void setProcessed(boolean processed) {
		this.processed = processed;
	}
	
	@ManyToMany(fetch=FetchType.EAGER, cascade=CascadeType.ALL)
	public List<Status> getStatuses() {
		return this.statuses;
	}
	
	@SuppressWarnings("unused")
	private void setStatuses(List<Status> statuses) {
		this.statuses = statuses;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((hash == null) ? 0 : hash.hashCode());
		result = prime * result + (processed ? 1231 : 1237);
		result = prime * result
				+ ((statuses == null) ? 0 : statuses.hashCode());
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
		Hash other = (Hash) obj;
		if (hash == null) {
			if (other.hash != null)
				return false;
		} else if (!hash.equals(other.hash))
			return false;
		if (processed != other.processed)
			return false;
		if (statuses == null) {
			if (other.statuses != null)
				return false;
		} else if (!statuses.equals(other.statuses))
			return false;
		return true;
	}
}
