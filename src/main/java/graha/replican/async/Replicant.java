package graha.replican.async;

import graha.replican.network.Consumer;
import graha.replican.network.UTFCoder;
import graha.replican.util.Constant;
import org.apache.mina.core.session.IoSession;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * <b>about</b>
 *   A destination where the file to be replicated
 * @author graha
 * @created 8/23/13 6:45 AM
 */
public class Replicant extends Consumer implements Runnable {

	private String location = "/tmp";
	private ReplicaLedger ledger = new ReplicaLedger();
	private BlockingQueue<String> replicas = new LinkedBlockingQueue<String>();

	public Replicant(int port){
		super (port);
	}

	/**
	 *
	 * Replicated Operations
	 *
	 * @param session
	 * @param message
	 * @throws Exception
	 */
	@Override
	public void messageReceived(IoSession session, Object message) throws Exception {
		String text = UTFCoder.decode(message);
		synchronized(message){
			String[] instructions = text.split(Constant.END_MSG);
			for (String instruction:instructions){
				replicas.add(instruction);
			}
		}
	}


	static void usage() {
		System.err.println("usage: java Replicant dir");
		System.exit(-1);
	}


	public void run() {
		while (true) {
			String instruction = null;
			try{
			instruction = replicas.poll(1, TimeUnit.SECONDS);
			if (instruction != null) {
				//System.out.println("Processing...." + instruction);
				Replica replica = ReplicaFactory.buildReplica(this.getLocation(), instruction);  //Auto digested
				System.out.println("Response :" + replica.toString());
				send(replica.toString());
			}
			}catch(Exception e){
				System.out.println(e.getMessage());
			}
			instruction = null; //Nullify
		}

	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public ReplicaLedger getLedger() {
		return ledger;
	}

	public void setLedger(ReplicaLedger ledger) {
		this.ledger = ledger;
	}

	public BlockingQueue<String> getReplicas() {
		return replicas;
	}

	public void setReplicas(BlockingQueue<String> replicas) {
		this.replicas = replicas;
	}

	public static void main(String args[]){
		if (args.length != 1)
			usage();

		Replicant replicant = new Replicant(12122);
		replicant.setLocation(args[0]);
		new Thread(replicant).start();
	}

}
