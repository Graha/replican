package graha.replican.async;

import graha.replican.checksum.RollingChecksum;
import graha.replican.network.Producer;
import graha.replican.network.UTFCoder;
import graha.replican.util.Constant;
import org.apache.mina.core.session.IoSession;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
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
		String[] operation = text.split(Constant.COLON);
		if (operation.length>=2){
			if(operation[0].equals("REQ_FILE")){
				this.send(readFile(operation[1]));
			}
			if(operation[0].equals("REQ_CK")){
				this.send(generateRollingChecksumAsString(readFile(operation[1])));
			}
		}


	}

	private String readFile( String file ) throws IOException {
		System.out.println("######## Reading "+ file);
		BufferedReader reader = new BufferedReader( new FileReader(file));
		String         line = null;
		StringBuilder  stringBuilder = new StringBuilder();
		String         ls = System.getProperty("line.separator");

		while( ( line = reader.readLine() ) != null ) {
			stringBuilder.append( line );
			stringBuilder.append( ls );
		}

		return stringBuilder.toString();
	}


	public List<Long> generateRollingChecksum(String file){

		List<Long> checksums = new ArrayList<Long>();

		int blockSize = Constant.BLOCK_SIZE;

		RollingChecksum checksum = new RollingChecksum(file, blockSize);

		int i=0;

		while (checksum.next()) {
			long c = checksum.weak();
			checksums.add(c);
			i++;
		}
		return checksums;
	}

	public String generateRollingChecksumAsString(String file){
		List<Long> ck = generateRollingChecksum(file);
		return Arrays.toString(ck.toArray());
	}


}
