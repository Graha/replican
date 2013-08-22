package graha.replican.message;

/**
 * <b>about</b>
 *
 * @author graha
 * @created 8/22/13 2:12 PM
 */


public interface Messenger {
	public void write(final String text);
	public void read(String text);
}
