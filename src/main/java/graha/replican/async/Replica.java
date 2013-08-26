package graha.replican.async;

import graha.replican.util.Constant;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * <b>about</b>
 *
 * @author graha
 * @created 8/26/13 4:58 PM
 */
public class Replica {
	public enum Operations {
		ENTRY_CREATE,		//source:Size:[File]
		ENTRY_MODIFY,       //source:CK#1,2,3,4,5:[FileByBlock] **Except last thers of fixed block size
		ENTRY_DELETE,       //source
		REPLY_CREATE,       //source:true:CK1,CK2,CK3.....
		REPLY_MODIFY, 		//source:true:CK1,CK2,CK3.....
		REPLY_DELETE 		//true
	};

	//Request
	private Long id;
	private String source;
	private Operations operations;
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

	public boolean digest(){

	return true;
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
}

