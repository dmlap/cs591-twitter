/**
 * 
 */
package edu.bu;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * @author dml
 * 
 */
public class StringOutputStreamFactory implements OutputStreamFactory {
	
	public Map<String, StringBuilderOutputStream> outputs = new HashMap<String, StringBuilderOutputStream>();

	public class StringBuilderOutputStream extends OutputStream {
		private final StringBuilder buffer = new StringBuilder();

		@Override
		public void write(int b) throws IOException {
			buffer.appendCodePoint(b);
		}

		public String asString() {
			return buffer.toString();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#toString()
		 */
		public String toString() {
			return asString();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.bu.OutputStreamFactory#open(java.lang.String)
	 */
	@Override
	public OutputStream open(String name) {
		StringBuilderOutputStream output = new StringBuilderOutputStream();
		outputs.put(name, output);
		return output;
	}

}
