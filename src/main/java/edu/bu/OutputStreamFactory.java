/**
 * 
 */
package edu.bu;

import java.io.OutputStream;

/**
 * An object which can create {@link OutputStream OutputStreams}.
 * 
 * @author dml
 * 
 */
public interface OutputStreamFactory {
	
	/**
	 * Constructs an {@link OutputStream} for the given identifier
	 * 
	 * @param name
	 *            - the identifier of the {@link OutputStream} to be created.
	 * @return a new {@link OutputStream}
	 */
	OutputStream open(String name);

}
