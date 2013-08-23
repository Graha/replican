package graha.replican.paxos;

/**
 * <b>about</b>
 *    Dummy hardcoded client
 *
 * @author graha
 * @created 8/23/13 3:21 AM
 */
public class Client{
	//TODO This Dirty code to be re-phrased

	public static void main (String argv[]){
		Leader lead = new Leader();
		lead.write(Long.toString(System.currentTimeMillis()), "data1");
		lead.write(Long.toString(System.currentTimeMillis()), "data2");
		lead.write(Long.toString(System.currentTimeMillis()), "data3");
		lead.write(Long.toString(System.currentTimeMillis()), "data4");
		lead.write(Long.toString(System.currentTimeMillis()), "data5");
		lead.write(Long.toString(System.currentTimeMillis()), "data6");
	}
}
