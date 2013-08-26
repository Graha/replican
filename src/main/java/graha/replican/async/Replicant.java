package graha.replican.async;

import graha.replican.network.Consumer;
import graha.replican.network.UTFCoder;
import graha.replican.util.Constant;
import org.apache.mina.core.session.IoSession;

import java.io.*;
import java.nio.file.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * <b>about</b>
 *   A destination where the file to be replicated
 * @author graha
 * @created 8/23/13 6:45 AM
 */
public class Replicant extends Consumer implements Runnable {

	private String pathPrefix = "/tmp";
	private BlockingQueue<Replica> replicas = new LinkedBlockingQueue<Replica>();

	public Replicant(int port){
		super (port);
	}

	/**
	 *
	 * Replicated Operations
	 *
	 * @param session
	 * @param message
	 * @throws Exception
	 */
	@Override
	public void messageReceived(IoSession session, Object message) throws Exception {
		String text = UTFCoder.decode(message);
		System.out.printf("Recieved : %s \n", text);
		replicateOperation(text);
	}


	static void usage() {
		System.err.println("usage: java Replicant dir");
		System.exit(-1);
	}

	public void setPathPrefix(String path){
		this.pathPrefix = path;
	}

	public void run(){

	}

	public void replicateOperation(String op){
		String[] opArray = op.split(Constant.COLON);
		String source = new String();  	// Real path
		String path = new String();     // Replicated path
		String Op = new String();       // Operation
		long size = 0;                  // File Size

		if (opArray.length < 2){
			System.err.println("Error in Operation");
			return;
		} else {
			//TODO Cleanup
			path = normalizePath(opArray[1]);
			Op = opArray[0];
			source = opArray[1];
			String[] strSize = opArray[2].split(Constant.END_MSG);
			size = Long.parseLong(strSize[0]);
			System.out.println("Opertion : " + Op);
			System.out.println("    File : " + source + " | " + path);
			System.out.println("    Size : " + size);
		}

		if(opArray[0].equals("ENTRY_CREATE")){   //Only File supported
			if(size == 0){
				try {
					Files.createFile(Paths.get(path));
				} catch (NoSuchFileException x) {
					System.err.format("%s: no such" + " file or directory%n", path);
				} catch (DirectoryNotEmptyException x) {
					System.err.format("%s not empty%n", path);
				} catch (IOException x) {
					// File permission problems are caught here.
				System.err.println(x);
				}
			}else {
				this.send(String.format("REQ_FILE:%s",source));
			}
		}else if (opArray[0].equals("ENTRY_DELETE")){
			try {
				System.out.println("Deleting " + path);
				Files.delete(Paths.get(path));
			} catch (NoSuchFileException x) {
				System.err.format("%s: no such" + " file or directory%n", path);
			} catch (DirectoryNotEmptyException x) {
				System.err.format("%s not empty%n", path);
			} catch (IOException x) {
				// File permission problems are caught here.
				System.err.println(x);
			}
		}else if (opArray[0].equals("ENTRY_MODIFY")){

		   this.send(String.format("REQ_CK:%s",source));	//Request for Checksum
		}
		/*
		else if (op.equals("CK"){
		 	this.generateAndCheck(checksum);
		}else if(op.equal("File"){
			this.writeFile(path, file);
		}
		 */
	}


	private void writeFile(String path, String file_content ) throws IOException {
		PrintWriter out = new PrintWriter(path);
		out.print(file_content);
		out.close();
	}

	public String normalizePath(String path){
		//TODO recursive to be supported
		String [] list = path.split(File.separator);
		return this.pathPrefix + File.separator + list[list.length-1];
	}


	public static void main(String args[]){
		if (args.length != 1)
			usage();

		Replicant replicant = new Replicant(12122);
		replicant.setPathPrefix(args[0]);
	}

}
