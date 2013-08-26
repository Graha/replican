package graha.replican.async;

import graha.replican.checksum.RollingChecksum;
import graha.replican.util.Constant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * <b>about</b>
 *
 * @author graha
 * @created 8/26/13 4:58 PM
 */
public class Replica {
	Logger log = LoggerFactory.getLogger(Replica.class);
	public static AtomicInteger IdGenerator = new AtomicInteger(0);
	private String location = "/tmp";

	public enum Operations {
		ENTRY_CREATE,		//Id:file:Size:[File]
		ENTRY_MODIFY,       //Id:file:CK#1,2,3,4,5:[FileByBlock] **Except last thers of fixed block size
		ENTRY_DELETE,       //Id:file
		REPLY_CREATE,       //file:true:CK1,CK2,CK3.....
		REPLY_MODIFY, 		//file:true:CK1,CK2,CK3.....
		REPLY_DELETE 		//true
	};

	//Request
	private long id;
	private String file;
	private Operations operations;
	private long size = 0;
	private List<TextBlock> blocks;

	//Responds
	private List<String> checksum;
	private Boolean isSuccess;

	public Replica(String text){
		this.digest(text);
	}

	public Replica(){}


	public synchronized boolean buildCreateResponse(List<String> checksum){
		//Keep ID, File Same
		this.operations = Operations.REPLY_CREATE;
		this.checksum = checksum;
		return true;
	}


	public synchronized boolean buildModifyResponse(List<TextBlock> changes){
		this.operations = Operations.REPLY_MODIFY;
		this.blocks = changes;
		return true;
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
		this.file = path.toString();
		return true;
	}


	public void digest(String text) {
		String[] splits = text.split(Constant.COLON);
		if (splits.length >= 3) {
			this.id = Long.parseLong(splits[0]);
			this.operations = Operations.valueOf(splits[1]);
			this.file = this.normalizePath(this.location, splits[2]);
			System.out.printf("%s %s %d \n ", file, this.operations.toString(), this.size);
			if (splits[1].equals("ENTRY_CREATE")) {
				this.size = Long.parseLong(splits[3]);
				try {
					//Create new fill
					Files.createFile(Paths.get(file));
					if (this.size > 0) {
						//Look for file content attached
						String content[] = text.split(Constant.START_FILE_MSG);
						if (content.length > 1) {
							//Fill content if any
							this.writeFile(this.file, content[1]);
							// Build Responds
							this.buildCreateResponse(this.generateRollingChecksum(content[1]));
						}
					}
				} catch (NoSuchFileException x) {
					System.out.println(String.format("%s: no such" + " file or directory", file));
				} catch (DirectoryNotEmptyException x) {
					System.out.println(String.format("%s not empty", file));
				} catch (IOException x) {
					// File permission problems are caught here.
					x.printStackTrace();
				}

			} else if (splits[1].equals("ENTRY_DELETE")) {
				try {
					Files.delete(Paths.get(file));
				} catch (NoSuchFileException x) {
					System.out.println(String.format("%s: no such" + " file or directory", file));
				} catch (DirectoryNotEmptyException x) {
					System.out.println(String.format("%s not empty", file));
				} catch (IOException x) {
					// File permission problems are caught here.
					System.out.println(x.getMessage());
				}
			} else if (splits[1].equals(Operations.ENTRY_MODIFY.name())) {

			} else if (splits[1].equals(Operations.REPLY_CREATE.name())) {
				String list[] =  splits[3].substring(1,  splits[3].length() - 1)
						.split(Constant.COMMA); // chop off brackets
				this.checksum = Arrays.asList(list);
			}
		}
	}


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
					this.size,Constant.START_FILE_MSG, fileContent);
		} else if (this.operations==Operations.ENTRY_DELETE){
			//file
			instruction = String.format("%d:%s:%s:%d", this.id,this.operations, this.file,
					this.size);
		} else if (this.operations==Operations.ENTRY_MODIFY){
			//file:CK#1,2,3,4,5:[FileByBlock] **Except last there of fixed block size
		} else if (this.operations==Operations.REPLY_CREATE){
			//file:true:CK1,CK2,CK3.....
			instruction = String.format("%d:%s:%s:%s", this.id,this.operations, this.file,
					this.checksum.toString());
			//System.out.println("Checkout : " + instruction);
		}

		return instruction + Constant.END_MSG;
	}

	//TODO: toByteArray()




	/**
	 *
	 *  File Operations
	 *
	 */


	public String normalizePath(String location, String file){
		//TODO recursive to be supported
		String [] list = file.split(File.separator);
		return location + File.separator + list[list.length-1];
	}

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


	private void writeFile(String path, String file_content ) throws IOException {
		PrintWriter out = new PrintWriter(path);
		out.print(file_content);
		out.close();
	}

	/**
	 *
	 * Checksum Calculations
	 *
	 */

	public List<String> generateRollingChecksum(){
		try{
			return this.generateRollingChecksum(readFile(this.getFile()));
		}catch(Exception e){
			System.out.println("Exception generating checksum");
		}
		return null;
	}

	public List<String> generateRollingChecksum(String file){

		List<String> checksums = new ArrayList<String>();

		int blockSize = Constant.BLOCK_SIZE;

		RollingChecksum checksum = new RollingChecksum(file, blockSize);

		int i=0;

		while (checksum.next()) {
			long c = checksum.weak();
			checksums.add(Long.toString(c));
			i++;
		}
		return checksums;
	}

	public String generateRollingChecksumAsString(String file){
		List<String> ck = generateRollingChecksum(file);
		return Arrays.toString(ck.toArray());
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

	public List<TextBlock> getBlocks() {
		return blocks;
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

	public void setBlocks(List<TextBlock> blocks) {
		this.blocks = blocks;
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

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}

	public List<String> getChecksum() {
		return checksum;
	}

	public void setChecksum(List<String> checksum) {
		this.checksum = checksum;
	}
}


class TextBlock{
	int block;
	String text = "";

	int getBlock() {
		return block;
	}

	void setBlock(int block) {
		this.block = block;
	}

	String getText() {
		return text;
	}

	void setText(String text) {
		this.text = text;
	}


}