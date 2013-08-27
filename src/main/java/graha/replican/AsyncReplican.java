package graha.replican;

import graha.replican.watch.DirectoryWatchService;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * <b>about</b>
 * <p/>
 * Simple implementation for Asynchronous Replication with DirectoryWarcher
 *
 * @author graha
 * @created 8/21/13 8:59 PM
 */

@Deprecated
public class AsyncReplican {

	static void usage() {
		System.err.println("usage: java AsyncReplican [-r] dir");
		System.exit(-1);
	}

	public static void main(String[] args) throws IOException {
		// parse arguments
		if (args.length == 0 || args.length > 2)
			usage();
		boolean recursive = false;
		int dirArg = 0;
		if (args[0].equals("-r")) {
			if (args.length < 2)
				usage();
			recursive = true;
			dirArg++;
		}

		//TODO Configurations to be added
		//ConfigurationFactory factory = new ConfigurationFactory("config.xml");
		//Configuration config = factory.getConfiguration();

		// register directory and process its events
		Path dir = Paths.get(args[dirArg]);
		new DirectoryWatchService(dir, recursive).start();
	}
}
