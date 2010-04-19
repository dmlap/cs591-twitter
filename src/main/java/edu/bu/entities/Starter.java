package edu.bu.entities;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Starter {
	private Long id;
	private int score;

 	private Starter() {}
	
	public static Starter createStarter(Long id, int score) {
		Starter result = new Starter();
		result.id = id;
		result.score = score;
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
	
	public int getScore() {
		return this.score;
	}
	
	public void setScore(int score) {
		this.score = score;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + score;
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
		Starter other = (Starter) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (score != other.score)
			return false;
		return true;
	}
}
