package edu.bu.entities;

import javax.persistence.Id;
import org.hibernate.annotations.*;

@Entity
public class Users {
	private Long id;
	
	public Users() {}
	
	public void setId(Long id) {
		this.id = id;
	}
	
	@Id
	public Long getId() {
		return this.id;
	}
}
