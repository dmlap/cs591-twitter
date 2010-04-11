/**
 * 
 */
package edu.bu;

/**
 * A generic, immutable container for two objects.
 * 
 * @author dml
 * 
 */
public class Pair<A, B> {
	public final A first;
	public final B second;
	
	public Pair(A first, B second) {
		this.first = first;
		this.second = second;
	}
	
	@Override
	public String toString() {
		return new StringBuilder("(").append(first.toString()).append(", ")
				.append(second.toString()).append(")").toString();
	}
}
