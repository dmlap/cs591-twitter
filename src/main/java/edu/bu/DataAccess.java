package edu.bu;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class DataAccess {

    public static boolean writeByteArray(byte[] output, String filename){
    	boolean success = false;
    	
    	try{
    		FileOutputStream fos = new FileOutputStream(filename);
    		fos.write(output);
    		fos.close();
    		
    		success = true;
    	}
    	catch(FileNotFoundException ex){
    		System.out.println("FileNotFoundException : " + ex);
    	}
    	catch(IOException ioe){
    		System.out.println("IOException : " + ioe);
    	}
    	
    	return success;
    }
    
    public static byte[] readByteArray(File file) throws IOException {
    	InputStream fis = new FileInputStream(file);
        
        // Get the size of the file
        long length = file.length();
    
        // Create the byte array to hold the data
        byte[] bytes = new byte[(int)length];
    
        // Read in the bytes
        int offset = 0;
        int numRead = 0;
        while (offset < bytes.length
               && (numRead = fis.read(bytes, offset, bytes.length - offset)) >= 0) {
            offset += numRead;
        }
    
        // Ensure all the bytes have been read in
        if (offset < bytes.length) {
            throw new IOException("Could not completely read file " + file.getName());
        }
    
        // Clean up
        fis.close();
        
        return bytes;
    }

}
