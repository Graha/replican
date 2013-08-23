package graha.replican.checksum;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

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
	
}
