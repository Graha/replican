package graha.replican.network;

import org.apache.mina.core.buffer.IoBuffer;

import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;

/**
 * <b>about</b>
 * Simpler UTF8 encoder decoder for Apache Mina IoBuffer
 *
 * @author graha
 * @created 8/23/13 1:26 AM
 */

public class UTFCoder {

	/**
	 * decode IoBuffer in to UTF8 String
	 *
	 * @param message
	 * @return
	 * @throws CharacterCodingException
	 */
	public static String decode(Object message) throws CharacterCodingException {
		IoBuffer buffer = (IoBuffer) message;
		String txt = buffer.getString(Charset.forName("UTF-8").newDecoder());
		buffer.flip();
		return txt;
	}


	/**
	 * encode UTF8 String in to IoBuffer
	 *
	 * @param text
	 * @return
	 * @throws CharacterCodingException
	 */
	public static IoBuffer encode(String text) throws CharacterCodingException {
		IoBuffer buffer = IoBuffer.allocate(text.length());
		buffer.putString(text, Charset.forName("UTF-8").newEncoder());
		buffer.flip();
		return buffer;
	}

}
