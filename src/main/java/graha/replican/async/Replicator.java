package graha.replican.async;

import graha.replican.checksum.MD5;
import graha.replican.network.Producer;
import graha.replican.network.UTFCoder;
import graha.replican.util.Constant;
import org.apache.mina.core.session.IoSession;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * <b>about</b>
 *   Source does the replication
 * @author graha
 * @created 8/23/13 6:46 AM
 */
public class Replicator extends Producer implements Runnable {

	private ReplicaLedger ledger = new ReplicaLedger();
	private BlockingQueue<String> replicas = new LinkedBlockingQueue<String>();
	private String incomplete = "";

	private String location = "/tmp";

	public Replicator(){
		super("localhost", 12122);
	}

	public Replicator(String location){
		this();
		this.setLocation(location);
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
		synchronized(message){
			String[] instructions = text.split(Constant.END_MSG);
			int start=0, end=instructions.length;

			if(!text.endsWith(Constant.END_MSG)){
				end = instructions.length-1;
				incomplete = incomplete + instructions[end];
			}else if(incomplete.length()>0){
				start=1;
				replicas.add(incomplete+instructions[0]);
				incomplete="";
			}

			for(int i=start; i<end;i++)
				replicas.add(instructions[i]);
		}
	}


	public void run() {
		while (true) {
			String instruction = null;
			try{
				instruction = replicas.poll(1, TimeUnit.SECONDS);
				if (instruction != null) {
					Replica replica = ReplicaFactory.buildReplica(this.getLocation(), instruction);  //Auto digested
					if (replica.getOperations().name().equals("REPLY_CREATE") ||
							replica.getOperations().name().equals("REPLY_MODIFY")){
						ledger.add(replica.getFile(), replica.getChecksum());
						 System.out.printf("Loaded %d,%s,%s\n", replica.getId(), replica.getFile(),
								 replica.getChecksum());
						List<String> list = replica.generateMD5Checksum();
						if (!replica.getOperations().name().equals("REPLY_MODIFY"))
							System.out.printf("Checksum for %s is %s\n", replica.getFile(), (MD5.compareChecksum(
								ledger.get(replica.getFile()),
								list))?"Matched":"Not Matched");

					}
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


	public void send(String location, String operation, Path path) {
		String instruction = null;
		Replica replica = ReplicaFactory.buildReplica(location);
		replica.buildRequest(operation, path);
		if (operation.equals("ENTRY_MODIFY"))
			sendDelta(replica);
		instruction = replica.toString();
		this.send(instruction);
	}

	public void sendDelta(Replica replica) {
		List<String> list = MD5.generateMD5Checksum(replica.getFile());
		List<Integer> delta = new ArrayList<Integer>();
		//Locate delta
		System.out.printf("Checksum for %s is %s\n", replica.getFile(), (MD5.compareChecksum(
				list, ledger.get(replica.getFile()),
				delta))?"Matched":"Not Matched");
		//ledger.add(replica.getFile(), list);
		replica.buildDelta(delta.get(0));
	}


}
