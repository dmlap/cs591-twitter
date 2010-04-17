package edu.bu.entities;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class LastID {
	private Long id;
	private String type;
	
	private LastID() {}
	
	public static LastID createLastID(Long id, String type) {
		LastID result = new LastID();
		result.id = id;
		result.type = type;
		
		return result;
	}
	
	public Long getId() {
		return this.id;
	}
	
	@SuppressWarnings("unused")
	private void setId(Long id) {
		this.id = id;
	}
	
	@Id
	public String getType() {
		return this.type;
	}
	
	@SuppressWarnings("unused")
	private void setType(String type) {
		this.type = type;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
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
		LastID other = (LastID) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}
}
