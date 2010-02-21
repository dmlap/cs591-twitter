package edu.bu;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import twitter4j.IDs;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;

public class DataAccess {

	public static void main( String[] args )
    {
    	TwitterFactory factory = new TwitterFactory();
    	Twitter twitter = factory.getInstance();
    	
    	try {
			IDs followers = twitter.getFollowersIDs("dlapalomento");
			int[] ids = followers.getIDs();
			
			for(int i = 0; i < ids.length; i++)
				System.out.println(ids[i]);
		} catch (TwitterException e) {
			e.printStackTrace();
		}
        
        String content = "Test byte array write";
        byte[] output = content.getBytes();
        String filename = "C:\\test.txt";
        
        boolean success = writeByteArray(output, filename);
        System.out.println(success);
        
        File file = new File(filename);
        byte[] contents = new byte[1];
        try {
			contents = readByteArray(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
        String readvalue = new String(contents);
        System.out.println(readvalue);
        
        System.exit(0);
    }
    
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
