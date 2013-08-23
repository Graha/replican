package graha.replican.paxos;

import graha.replican.network.Producer;
import graha.replican.network.UTFCoder;
import graha.replican.util.Constant;
import org.apache.mina.core.session.IoSession;

import java.nio.charset.CharacterCodingException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * <b>about</b>
 *    Simple implementation of paxos' Leader
 *  (Normally party of Acceptor group)
 *
 * @author graha
 * @created 8/23/13 1:54 AM
 */
public class Leader extends Producer {


	//TODO load all possible Acceptor from configuration or introduce discovery service
	private String value = new String();
	private String key = new String();
	private boolean _transactionComplete = true; //TODO replaced with TRUE TIME
	private BlockingQueue<Pair> queue = new LinkedBlockingDeque<Pair>();   /// Waiting QUEUE


	/**
	 *
	 * Leader/Proposer Protocol been defined here
	 *
	 * @param session
	 * @param message
	 * @throws Exception
	 */
	@Override
	public void messageReceived(IoSession session, Object message) throws Exception {
		String text = UTFCoder.decode(message);
		System.out.printf("Recieved : %s \n", text);
		String[] parameter = parse(text);
		if(parameter.length>=1){
			if (parameter[0].equalsIgnoreCase("prepared")) {
				this.sendAccept(key, value);
			} else if (parameter[0].equalsIgnoreCase("accepted")) {
				try {
					this.write(queue.take());
				} catch (InterruptedException e) {
					System.out.println("Queue Empty");
					_transactionComplete = true;
				}
			}  else {
				try {
					this.write(queue.take());
				} catch (InterruptedException e) {
					System.out.println("Queue Empty");
					_transactionComplete = true;
				}
			}
		}
	}



	private String[] parse (String text){
		String[] arguments = text.split(Constant.COLON);
		return arguments;
	}

	/**
	 *
	 * Write the Key-Value pair in to System
	 *
	 * @param Pair<Key, Value>
	 *
	 * @return
	 */
	private boolean write (Pair KV){
		this.value = String.format("%s,%s", KV.getKey(), KV.getValue());
		this.sendPrepare(key); // QUORUMS size to be defined Total/2+1
		_transactionComplete = false;
		return true;
	};


	/**
	 *
	 * Reading by Key for Value from System
	 *
	 * @param key
	 *
	 * @return
	 */
	private boolean read (String key){
		throw new UnsupportedOperationException("Read Not Supported Yet");
	};

	/**
	 *
	 * Write the Key-Value pair in to System
	 *
	 * @param key
	 * @param value
	 * @return
	 */
	public boolean write(String key, String value){
		this.key = key;
		if (_transactionComplete) {
			this.value = String.format("%s,%s", key, value);
			this.sendPrepare(key); // QUORUMS size to be defined Total/2+1
			_transactionComplete = false;
		} else {
			try {
				queue.put(new Pair(key, value));
			} catch (InterruptedException e) {
				//TODO
			}
		}
		return true;
	}


	/**
	 *
	 * Sending Prepare Signal to Acceptor
	 *
	 * @param key
	 */
	private void sendPrepare(String key){
		String prepare = String.format("prepare:%s\n", key);
		System.out.println(prepare);
		try {
			session.write(UTFCoder.encode(prepare));
		} catch (CharacterCodingException e) {
			throw new RuntimeException(prepare+" failed");
		}
	}


	/**
	 *
	 * Sending Accept Request to Acceptor
	 *
	 * @param key
	 * @param value
	 */
	private void sendAccept(String key, Object value){
		String accept = String.format("accept:%s:%s\n",key, value.toString());
		System.out.println(accept);

		try {
			session.write(UTFCoder.encode(accept));
		} catch (CharacterCodingException e) {
			throw new RuntimeException(accept+" failed");
		}	}

}

class Pair {
	private String key;
	private String value;

	public Pair(String k, String v){
		key = k;
		value = v;
	}

	public String getKey(){
		return key;
	}

	public String getValue(){
		return value;
	}
}