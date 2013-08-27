package graha.replican.async;

import graha.replican.util.Constant;
import org.apache.log4j.Logger;

import java.io.*;
import java.nio.file.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * <b>about</b>
 *   Class to manages Replica's CRUD and Wrappers
 * @author graha
 * @created 8/26/13 4:58 PM
 */
public class Replica {
	Logger log = Logger.getLogger(Replica.class);
	public static AtomicInteger IdGenerator = new AtomicInteger(0);
	private String location = "/tmp";

	public enum Operations {
		ENTRY_CREATE,		//Id:file:CP#:[File]
		ENTRY_MODIFY,       //Id:file:CP#:[Appends]
		ENTRY_DELETE,       //Id:file
		REPLY_CREATE,       //file:true:CP#
		REPLY_MODIFY, 		//file:true:CP#
		REPLY_DELETE 		//true
	};

	//Request
	private long id;
	private String file;
	private Operations operations;
	private String delta;
	private long checkpoint;

	//Responds
	private Boolean isSuccess;

	public Replica(String text){
		this.digest(text);
	}

	public Replica(){}

	/**
	 *
	 * Building Response for Create
	 *
	 * @param checkpoint
	 * @return
	 */


	public synchronized boolean buildCreateResponse(long checkpoint){
		//Keep ID, File Same
		this.operations = Operations.REPLY_CREATE;
		this.checkpoint = checkpoint;
		return true;
	}


	/**
	 *
	 * Building Response for Modify
	 *
	 * @param checkpoint
	 * @return
	 */

	public synchronized boolean buildModifyResponse(long checkpoint){
		this.operations = Operations.REPLY_MODIFY;
		this.checkpoint = checkpoint;
		return true;
	}


	/**
	 *
	 * Building Response for Delete
	 *
	 * @param success
	 * @return
	 */
	public synchronized boolean buildDeleteResponse(boolean success){
		//Keep ID, File Same
		this.operations = Operations.REPLY_DELETE;
		this.setSuccess(success);
		return true;
	}


	/**
	 *
	 * Building Request object for Replica
	 *
	 * @param operation
	 * @param path
	 * @return
	 */
	public synchronized boolean buildRequest(String operation, Path path){
		this.id = IdGenerator.getAndIncrement();
		try {
		this.checkpoint = (!operation.equals("ENTRY_DELETE"))
				? Files.size(path):0;
		}catch(IOException e){
			log.info("Size calculation failed for " + path.toString());
		}
		this.operations = Operations.valueOf(operation);
		this.file = path.toString();
		return true;
	}

	/**
	 *
	 * Replication instruction digester
	 * @param text
	 *
	 */

	public void digest(String text) {
		String[] splits = text.split(Constant.COLON);
		if (splits.length >= 3) {
			this.id = Long.parseLong(splits[0]);
			this.operations = Operations.valueOf(splits[1]);
			log.info("Working on : "+ this.operations.name());
			this.file = this.normalizePath(this.location, splits[2]);
			if (splits[1].equals("ENTRY_CREATE")) {
				this.checkpoint = Long.parseLong(splits[3]);
				try {
					//Create new fill
					Files.createFile(Paths.get(file));
					log.info("Created " + file);
					if (this.checkpoint > 0) {
						//Look for file content attached
						String content[] = text.split(Constant.START_FILE_MSG);
						if (content.length > 1) {
							//Fill content if any
							this.writeFile(this.file, content[1]);
							// Build Responds
							this.buildCreateResponse(this.checkpoint);
						}
					}
				} catch (NoSuchFileException x) {
					log.warn(String.format("%s: no such" + " file or directory", file));
				} catch (DirectoryNotEmptyException x) {
					log.warn(String.format("%s not empty", file));
				} catch (IOException x) {
					// File permission problems are caught here.
					x.printStackTrace();
				}

			} else if (splits[1].equals("ENTRY_DELETE")) {
				try {
					Files.delete(Paths.get(file));
					this.buildDeleteResponse(true);
				} catch (NoSuchFileException x) {
					log.warn(String.format("%s: no such" + " file or directory", file));
				} catch (DirectoryNotEmptyException x) {
					log.warn(String.format("%s not empty", file));
				} catch (IOException x) {
					// File permission problems are caught here.
					log.error(x.getMessage());
				}
			} else if (splits[1].equals("ENTRY_MODIFY")) {
				//file:CP:[FileByBlock] **Except last there of fixed block size
				this.setCheckpoint(Integer.parseInt(splits[3]));
				try{
				if (this.checkpoint > 0) {
					//Look for file content attached
					String content[] = text.split(Constant.START_FILE_MSG);
					if (content.length > 1) {
						//Fill content if any
						this.setDelta(content[1]);
						this.writeFileDelta(this.file, delta);
						// Build Responds
						this.buildModifyResponse(this.getCheckpoint());
					}
				}
			} catch (NoSuchFileException x) {
				log.warn(String.format("%s: no such" + " file or directory", file));
			} catch (DirectoryNotEmptyException x) {
				log.warn(String.format("%s not empty", file));
			} catch (IOException x) {
				// File permission problems are caught here.
				x.printStackTrace();
			}

		} else if (splits[1].equals(Operations.REPLY_CREATE.name()) ||
					splits[1].equals(Operations.REPLY_MODIFY.name())) {
				int ck =  Integer.parseInt(splits[3]);
				this.checkpoint = ck;
			}
		}
	}

	/**
	 *
	 * Building instruction out of Replica object
	 *
	 * @return
	 */

	public String toString(){
		String instruction = "";
		String fileContent = "";
		if (this.operations==Operations.ENTRY_CREATE){
			//file:Size:[File]
			try {
				fileContent = this.readFile(this.file);
			}catch (Exception e){
				log.error("Reading file failed for " + e.getMessage());
			}
			instruction = String.format("%d:%s:%s:%d:%s%s", this.id, this.operations, this.file,
					this.checkpoint,Constant.START_FILE_MSG, fileContent);
		} else if (this.operations==Operations.ENTRY_DELETE){
			//file
			instruction = String.format("%d:%s:%s:%d", this.id,this.operations, this.file,
					this.checkpoint);
		} else if (this.operations==Operations.ENTRY_MODIFY){
			//file:DELTA:[FileByBlock] **Except last there of fixed block size
			instruction = String.format("%d:%s:%s:%d:%s%s", this.id, this.operations, this.file,
					this.checkpoint,Constant.START_FILE_MSG, this.getDelta());
			log.debug("Checkout : " + instruction);
		} else if (this.operations==Operations.REPLY_CREATE ||
				this.operations==Operations.REPLY_MODIFY){
			//file:true:CP
			instruction = String.format("%d:%s:%s:%s", this.id,this.operations, this.file,
					this.checkpoint);
			log.debug("Checkout : " + instruction);
		}

		return instruction + Constant.END_MSG;
	}

	//TODO: toByteArray()




	///////// File Operations


	/**
	 *
	 * Normalize path to convert between remote and local replica folders
	 *
	 * @param location
	 * @param file
	 * @return
	 */
	public String normalizePath(String location, String file){
		//TODO recursive to be supported
		String [] list = file.split(File.separator);
		return location + File.separator + list[list.length-1];
	}

	/**
	 *
	 * Reading whole file for creation time
	 *
	 * @param file
	 * @return
	 * @throws IOException
	 */

	private String readFile( String file ) throws IOException {
		//System.out.println("######## Reading "+ file);
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

	/**
	 *
	 * Reading only delta changes
	 *
	 * @param file
	 * @param checkpoint
	 * @return
	 * @throws IOException
	 */

	private String readFileDelta( String file, long checkpoint ) throws IOException {
		//Byte stream handling
		BufferedReader reader = new BufferedReader( new FileReader(file));
		String         line = null;
		StringBuilder  stringBuilder = new StringBuilder();
		String         ls = System.getProperty("line.separator");

		reader.skip(checkpoint);
		while( ( line = reader.readLine() ) != null ) {
			stringBuilder.append( line );
			stringBuilder.append(ls);
		}
		log.info("Found Delta : \n" +stringBuilder.toString());
		return stringBuilder.toString();
	}


	/**
	 *
	 * write the whole file
	 *
	 * @param path
	 * @param file_content
	 * @throws IOException
	 */

	private void writeFile(String path, String file_content ) throws IOException {
		PrintWriter out = new PrintWriter(path);
		out.print(file_content);
		out.close();
	}


	/**
	 *
	 * append only aggregation
	 *
	 * @param path
	 * @param file_content
	 * @throws IOException
	 */
	private void writeFileDelta(String path, String file_content) throws IOException {
		RandomAccessFile reader = new RandomAccessFile(new File(path.toString()), "rw");
		String         line = null;
		String         ls = System.getProperty("line.separator");
		reader.seek(this.checkpoint);
		reader.writeBytes(file_content);
		reader.close();
	}



	public Long getId() {
		return id;
	}

	public String getFile() {
		return file;
	}

	public Operations getOperations() {
		return operations;
	}


	public Boolean getSuccess() {
		return isSuccess;
	}



	public void setId(Long id) {
		this.id = id;
	}

	public void setFile(String file) {
		this.file = file;
	}

	public void setOperations(Operations operations) {
		this.operations = operations;
	}


	public void setSuccess(Boolean success) {
		isSuccess = success;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public void setId(long id) {
		this.id = id;
	}


	public String getDelta() {
		return delta;
	}

	public void setDelta(String delta) {
		this.delta = delta;
	}

	public long getCheckpoint() {
		return checkpoint;
	}

	public void setCheckpoint(long checkpoint) {
		this.checkpoint = checkpoint;
	}

	public void buildDelta(long checkpoint){
		try {
			this.setCheckpoint(checkpoint);
			this.setDelta(readFileDelta(this.file, checkpoint));
		}catch(Exception e){
			log.warn("Delta Blocking unsuccessful");
		}
	}
}
