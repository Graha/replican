package graha.replican.async;

/**
 * <b>about</b>
 *
 * @author graha
 * @created 8/26/13 11:42 PM
 */
public class ReplicaFactory {
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
