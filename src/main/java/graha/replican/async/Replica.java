package graha.replican.async;

import graha.replican.util.Constant;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * <b>about</b>
 *
 * @author graha
 * @created 8/26/13 4:58 PM
 */
public class Replica {
	public Logger log = Logger.getLogger(Replica.class);
	public static AtomicInteger IdGenerator = new AtomicInteger(0);

	public enum Operations {
		ENTRY_CREATE,		//source:Size:[File]
		ENTRY_MODIFY,       //source:CK#1,2,3,4,5:[FileByBlock] **Except last thers of fixed block size
		ENTRY_DELETE,       //source
		REPLY_CREATE,       //source:true:CK1,CK2,CK3.....
		REPLY_MODIFY, 		//source:true:CK1,CK2,CK3.....
		REPLY_DELETE 		//true
	};

	//Request
	private long id;
	private String source;
	private Operations operations;
	private long size = 0;
	private List<ByteBuffer> blocks;

	//Responds
	private List<Long> checksum;
	private Boolean isSuccess;



	public Long getId() {
		return id;
	}

	public String getSource() {
		return source;
	}

	public Operations getOperations() {
		return operations;
	}

	public List<ByteBuffer> getBlocks() {
		return blocks;
	}

	public List<Long> getChecksum() {
		return checksum;
	}

	public Boolean getSuccess() {
		return isSuccess;
	}

	public synchronized boolean buildRequest(String operation, Path path){
		this.id = IdGenerator.getAndIncrement();
		try {
		this.size = (!operation.equals("ENTRY_DELETE"))
				? Files.size(path):0;
		}catch(IOException e){
			log.info("Size calculation failed for " + path.toString());
		}
		this.operations = Operations.valueOf(operation);
		this.source = path.toString();

	return true;
	}

	public String toString(){
		String instruction = "";
		String fileContent = "";
		if (this.operations==Operations.ENTRY_CREATE){
			//source:Size:[File]
			try {
				fileContent = this.readFile(this.source);
			}catch (Exception e){
				log.error("Reading file failed for " + e.getMessage());
			}
			instruction = String.format("%d:%s:%d:%s", this.id, this.source,
					this.size, fileContent);
		} else if (this.operations==Operations.ENTRY_DELETE){
			//source
			instruction = String.format("%d:%s:%d", this.id, this.source,
					this.size);
		} else if (this.operations==Operations.ENTRY_MODIFY){
			//source:CK#1,2,3,4,5:[FileByBlock] **Except last thers of fixed block size
		}

		return instruction+Constant.END_MSG;
	}

	//TODO: toByteArray()

	/**
	 *
	 *  File Operations
	 *
	 */

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


	public void setId(Long id) {
		this.id = id;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public void setOperations(Operations operations) {
		this.operations = operations;
	}

	public void setBlocks(List<ByteBuffer> blocks) {
		this.blocks = blocks;
	}

	public void setChecksum(List<Long> checksum) {
		this.checksum = checksum;
	}

	public void setSuccess(Boolean success) {
		isSuccess = success;
	}


	public void execute(){
		if(operations.equals(Operations.ENTRY_CREATE)){   //Only File supported
			if(size == 0){
				try {
					Files.createFile(Paths.get(source));
				} catch (NoSuchFileException x) {
					log.error(String.format("%s: no such" + " file or directory%n", source));
				} catch (DirectoryNotEmptyException x) {
					log.error(String.format("%s not empty%n", source));
				} catch (IOException x) {
					// File permission problems are caught here.
					log.error(x);
				}
			}
		}else if (operations.equals(Operations.ENTRY_DELETE)){
			try {
				Files.delete(Paths.get(source));
			} catch (NoSuchFileException x) {
				log.error(String.format("%s: no such" + " file or directory%n", source));
			} catch (DirectoryNotEmptyException x) {
				log.error(String.format("%s not empty%n", source));
			} catch (IOException x) {
				// File permission problems are caught here.
				log.error(x);
			}
		}else if (operations.equals(Operations.ENTRY_MODIFY)){

		}

	}

}

