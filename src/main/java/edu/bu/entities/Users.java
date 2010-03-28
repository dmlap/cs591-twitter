package edu.bu.entities;

import javax.persistence.Id;
import org.hibernate.annotations.*;

@Entity
public class Users {
	private Long id;
	private String name;
	private int degree;

 	public Users() {}
	
	public void createUser(Long id, String name, int degree) {
		this.id = id;
		this.name = name;
		this.degree = degree;
	}
	
	@Id
	public Long getId() {
		return this.id;
	}
	
	public String getName() {
		return this.name;
	}
	
	public int getDegree() {
		return this.degree;
	}
}
