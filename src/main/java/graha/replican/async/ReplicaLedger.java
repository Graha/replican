package graha.replican.async;

import org.apache.log4j.Logger;

import java.util.HashMap;

/**
 * <b>about</b>
 *
 * @author graha
 * @created 8/26/13 9:58 PM
 */
public class ReplicaLedger {
	public Logger log = Logger.getLogger(ReplicaLedger.class);
	HashMap<String, Long> ledger = new HashMap<String, Long>(); //String just for ease of use

	public void add(String file, long lastWritten){
		ledger.put(file, lastWritten);
	}

	public Long get(String file){
		if (ledger.containsKey(file)){
			return ledger.get(file);
		} else {
			log.warn("Reading all content, in case of inconsistency");
			return 0L; // Read all again
		}
	}
}
