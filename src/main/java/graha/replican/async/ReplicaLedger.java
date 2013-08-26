package graha.replican.async;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * <b>about</b>
 *
 * @author graha
 * @created 8/26/13 9:58 PM
 */
public class ReplicaLedger {
	public Logger log = Logger.getLogger(ReplicaLedger.class);
	HashMap<String,List<Long>> ledger = new HashMap<String, List<Long>>();

	public void add(String file, List<Long> checksum){
		ledger.put(file, checksum);
	}

	public List<Long> get(String file){
		if (!ledger.containsKey(file)){
			return ledger.get(file);
		} else {
			log.error(file + " not found");
			return new ArrayList<Long>();
		}
	}
}
