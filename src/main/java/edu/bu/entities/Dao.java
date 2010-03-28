/**
 * 
 */
package edu.bu.entities;

/**
 * A generic data access object for <code>T</code>s.
 * 
 * @author dml
 * 
 */
public interface Dao<T, K> {

	/**
	 * Returns the <code>T</code> with the specified key
	 * 
	 * @param key
	 *            - the primary key to search for
	 * @return the <code>T</code> with the specified key
	 */
	T get(K key);

	/**
	 * Persists the specified target
	 * 
	 * @param target
	 *            - the object to be persisted
	 */
	void save(T target);
	
	/**
	 * Deletes the persistent representation of the specified <code>T</code>
	 * 
	 * @param target
	 *            - the object to be deleted
	 */
	void delete(T target);

}
