package graha.replican.async;

import graha.replican.checksum.RollingChecksum;
import graha.replican.network.Producer;
import graha.replican.network.UTFCoder;
import graha.replican.util.Constant;
import org.apache.mina.core.session.IoSession;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.CharacterCodingException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * <b>about</b>
 *   Source does the replication
 * @author graha
 * @created 8/23/13 6:46 AM
 */
public class Replicator extends Producer {


	ReplicaLedger ledger = new ReplicaLedger();

	public Replicator(){
		super("localhost", 12122);
	}

	public Replicator(String host, int port){
		   super(host, port);
	}

	/**
	 *
	 * Operations Responds
	 *
	 * @param session
	 * @param message
	 * @throws Exception
	 */
	@Override
	public void messageReceived(IoSession session, Object message) throws Exception {
		String text = UTFCoder.decode(message);
		System.out.printf("Recieved : %s \n", text);
	}

	public void send(String location, String operation, Path path) {
		String instruction = null;
		Replica replica = ReplicaFactory.buildReplica(location);
		replica.buildRequest(operation, path);
		instruction = replica.toString();
		System.out.printf("Looking @ %s %s %s\n", location, operation, path.toString());
		System.out.println("Generated : "+ instruction);
		this.send(instruction);
	}

}
