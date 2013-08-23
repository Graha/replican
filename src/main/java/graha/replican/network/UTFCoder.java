package graha.replican.network;

import org.apache.mina.core.buffer.IoBuffer;

import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;

/**
 * <b>about</b>
 *
 * @author graha
 * @created 8/23/13 1:26 AM
 */
public class UTFCoder {

	public static String  decode(Object message) throws CharacterCodingException {
		IoBuffer buffer = (IoBuffer) message;
		String txt = buffer.getString(Charset.forName("UTF-8").newDecoder());
		buffer.flip();
		return txt;
	}

	public static IoBuffer encode(String text) throws CharacterCodingException {
		IoBuffer buffer = IoBuffer.allocate(text.length());
		buffer.putString(text, Charset.forName("UTF-8").newEncoder());
		buffer.flip();
		return buffer;
	}

}
