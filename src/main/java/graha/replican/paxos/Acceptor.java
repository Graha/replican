package graha.replican.paxos;

import graha.replican.network.Consumer;
import graha.replican.network.UTFCoder;
import graha.replican.util.Constant;
import org.apache.mina.core.session.IoSession;

import java.io.IOException;
import java.nio.charset.CharacterCodingException;
import java.util.Map;

/**
 * <b>about</b>
 *
 * @author graha
 * @created 8/23/13 1:52 AM
 */
public class Acceptor extends Consumer {

	 // Control to Learner Reader / Writer in here.
	 Learner learn = new Learner();


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void messageReceived(IoSession session, Object message) throws Exception {
		String text = UTFCoder.decode(message);
		System.out.printf("Recieved : %s \n", text);
		String[] parameter = parse(text);
		if(parameter.length>=2){
			if (parameter[0].equalsIgnoreCase("prepare")) {
				this.sendPromise(parameter[1]);
			} else if (parameter[0].equalsIgnoreCase("accept")) {
				this.sendAccepted(parameter[1]);
				learning(parameter[2]);
			}
		}
	}

	private String[] parse (String text){
		String[] arguments = text.split(Constant.COLON);
		return arguments;
	}

	/**
	 *
	 * Pass the control to learner as it clears distribution hurdles
	 *
	 * @param text
	 * @return
	 */
	private boolean learning(String text){
		String[] kv = text.split(Constant.COMMA);
		if (kv.length == 2) {
			learn.put(kv[0], kv[1]);
		}  else {
			System.out.println("learning failed");
			return false;
		}
		System.out.printf("Learning Successful %s -> %s\n", kv[0], kv[1]);
		return true;
	}


	/**
	 *
	 * Sends Promise as result of Proposal
	 *
	 * @param key
	 */
	private void sendPromise(String key){
		String prepare = String.format("prepared:%s\n",key);
		System.out.println(prepare);
		try {
			Map<Long, IoSession> map = acceptor.getManagedSessions();
			for (Long sessionId : map.keySet()){
				map.get(sessionId).write(UTFCoder.encode(prepare));
			}
		} catch (CharacterCodingException e) {
			throw new RuntimeException(prepare+" failed");
		}
	}


	/**
	 *
	 * Sends Accepted Signal back as result of Accept Request
	 *
	 * @param key
	 */
	private void sendAccepted(String key){
		String accept = String.format("accepted:%s\n",key);
		System.out.println(accept);
		try {
			Map<Long, IoSession> map = acceptor.getManagedSessions();
			for (Long sessionId : map.keySet()){
				map.get(sessionId).write(UTFCoder.encode(accept));
			}
		} catch (CharacterCodingException e) {
			throw new RuntimeException(accept+" failed");
		}
	}


	/**
	 * The entry point.
	 */
	public static void main(String[] args) throws IOException {
		Acceptor server = new Acceptor();

	}

}
