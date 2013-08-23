package graha.replican.checksum;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

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

}
