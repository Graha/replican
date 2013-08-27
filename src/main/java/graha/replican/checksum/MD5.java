package graha.replican.checksum;

import graha.replican.util.Constant;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

/**
 * <b>about</b>
 * <p/>
 * Implementation for MD5, strong digester and checksum calculation
 *
 * @author graha
 * @created 8/21/13 8:59 PM
 */

public class MD5 {

	private static MessageDigest md5;

	static {
		try {
			md5 = MessageDigest.getInstance("md5");
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("Failed to initialize MD5 MessageDigest format", e);
		}
	}

	public static final byte[] digest(byte[] data) {
		md5.reset();
		return md5.digest(data);
	}


	public static String digest(String md5) {
		try {
			java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
			byte[] array = md.digest(md5.getBytes());
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < array.length; ++i) {
				sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1,3));
			}
			return sb.toString();
		} catch (java.security.NoSuchAlgorithmException e) {
		}
		return null;
	}


	public static boolean compareChecksum(List<String> src, List<String> tgt) {
		boolean flag = true;
		int min = (src.size()>tgt.size())?tgt.size():src.size();
		for (int i = 0; i < min; i++) {
			if (!tgt.get(i).trim().equals(src.get(i).trim())) {
				System.out.printf("Not Matching %d\n", i);
				flag =false;
			}
		}
		return flag;
	}


	public static boolean compareChecksum(final List<String> src, final List<String> cmpre, List<Integer> delta) {
		boolean flag = true;
		//TODO need a better idea      meant only for appending file
		int min = (src.size()<cmpre.size())?src.size():cmpre.size();
		for (int i = 0; i < min; i++) {
			if (!cmpre.get(i).trim().equals(src.get(i).trim())) {
				delta.add(i);
				flag =false;
			}
		}
		for (int i=min; i<src.size();i++){
			delta.add(i);
		}
		System.out.printf("Delta %s\n", delta.toString());
		return flag;
	}



	public static List<String> generateMD5Checksum(String file){

		try {
			System.out.printf("Building Checksum for %s of size %d\n", file, Files.size(Paths.get(file)));
		}catch(Exception e){

		}
		List<String> checksums = new ArrayList<String>();
		try{
			BufferedReader reader = new BufferedReader( new FileReader(file));
			String         line = null;
			StringBuilder  stringBuilder = new StringBuilder();
			String         ls = System.getProperty("line.separator");

			int i=0;
			while( ( line = reader.readLine() ) != null ) {
				stringBuilder.append( line );
				stringBuilder.append( ls );
				i++;
				if (i>= Constant.BLOCK_SIZE){
					checksums.add(MD5.digest(stringBuilder.toString()));
					stringBuilder.setLength(0); //Reset stringBuilder
					i=0; //Reset counter
				}
			}
			checksums.add(MD5.digest(stringBuilder.toString())); // Remaining lines to be checksum
			reader.close();
		}catch(Exception e){
			System.out.println("Error reading file " + e.getMessage());
		}

		return checksums;
	}



}
