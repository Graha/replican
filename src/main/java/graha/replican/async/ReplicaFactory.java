package graha.replican.async;

import org.apache.log4j.Logger;

/**
 * <b>about</b>
 *		Factory for replica object creation
 *
 * @author graha
 * @created 8/26/13 11:42 PM
 */
public class ReplicaFactory {
	Logger log = Logger.getLogger(ReplicaFactory.class);

	public static Replica buildReplica(String prefix){
		Replica replica = new Replica();
		replica.setLocation(prefix);
		return replica;
	}


	public static Replica buildReplica(String prefix, String instruction){
		Replica replica = new Replica();
		replica.setLocation(prefix);
		replica.digest(instruction);
		return replica;
	}

}
