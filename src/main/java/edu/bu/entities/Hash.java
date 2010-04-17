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
	private List<Status> statuses;
	
	private Hash() {}
	
	public static Hash createHash(String hash, List<Status> statuses) {
		Hash result = new Hash();
		result.hash = hash;
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
		return true;
	}
}
