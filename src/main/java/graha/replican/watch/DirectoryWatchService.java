package graha.replican.watch;

import graha.replican.async.Replicator;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static java.nio.file.StandardWatchEventKinds.*;


/**
 * <b>about</b>
 *			***** Needs Java 1.7 ******
 * 		Add an Watch for specified Directory for listening Changes in it
 *
 * @author graha
 * @created 8/21/13 7:59 PM
 */

public class DirectoryWatchService extends Thread {

	private final WatchService watcher;
	private final Map<WatchKey,Path> keys;
	private final boolean recursive;
	private boolean trace = false;
	private Replicator replicator = null;
	private Path path = null;

	@SuppressWarnings("unchecked")
	static <T> WatchEvent<T> cast(WatchEvent<?> event) {
		return (WatchEvent<T>)event;
	}

	/**
	 * Register a Single Directory for Watching Changes
	 *
	 * @param dir
	 * @throws IOException
	 */
	private void register(Path dir) throws IOException {
		WatchKey key = dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
		if (trace) {
			Path prev = keys.get(key);
			if (prev == null) {
				System.out.format("register: %s\n", dir);
			} else {
				if (!dir.equals(prev)) {
					System.out.format("update: %s -> %s\n", prev, dir);
				}
			}
		}
		keys.put(key, dir);
	}


	/**
	 * Register Directory and sub-Directory Recursively for watch
	 *
	 * @param start
	 * @throws IOException
	 */
	private void registerDeep(final Path start) throws IOException {
		// register directory and sub-directories
		Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
					throws IOException {
				register(dir);
				return FileVisitResult.CONTINUE;
			}
		});
	}

	/**
	 * Creates a Watcher and registers the given directory
	 *
	 * @param dir
	 * @param recursive
	 * @throws IOException
	 */
	public DirectoryWatchService(Path dir, boolean recursive) throws IOException {
		this.watcher = FileSystems.getDefault().newWatchService();
		this.keys = new HashMap<WatchKey,Path>();
		this.recursive = recursive;
		this.path = dir;
		this.replicator = new Replicator(dir.toString());
		new Thread(this.replicator).start();

		if (recursive) {
			System.out.format("Scanning %s ...\n", dir);
			registerDeep(dir);
			System.out.println("Done.");
		} else {
			register(dir);
		}

		// enable trace after initial registration
		this.trace = true;
	}

	@Override
	public void run(){
		this.processEvent();
	}



	/**
	 *
	 * Process all events for keys queued to the watcher
	 *
	 */
	void processEvent() {
		for (;;) {

			// wait for key to be signalled
			WatchKey key;
			try {
				key = watcher.take();
			} catch (InterruptedException x) {
				return;
			}

			Path dir = keys.get(key);
			if (dir == null) {
				System.err.println("WatchKey not recognized!!");
				continue;
			}

			for (WatchEvent<?> event: key.pollEvents()) {
				WatchEvent.Kind kind = event.kind();

				// TBD - provide example of how OVERFLOW event is handled
				if (kind == OVERFLOW) {
					continue;
				}

				// Context for directory entry event is the file name of entry
				WatchEvent<Path> ev = cast(event);
				Path name = ev.context();
				Path child = dir.resolve(name);

				String instruction = "";
				try{
					// Send it out
					//Long size = (!event.kind().name().equals("ENTRY_DELETE"))
					//		?Files.size(child):0;
					//instruction =
					//		String.format("%s:%s:%d#&#", event.kind().name(), child, size);
					// event.kind().name(), child
					//System.out.println("Generated : "+ instruction);
					//TO ignore swp files
					//String ext[] = child.toString().split(".");
					//if (ext[ext.length-1]!="swp")
					System.out.println(String.format("%s:%s", event.kind().name(), child));
					replicator.send(dir.toString(), event.kind().name(), child);
				}catch (Exception e){
					e.printStackTrace();
					System.out.println ("Replication failed for "+ instruction);
				}

				// if directory is created, and watching recursively, then
				// register it and its sub-directories
				if (recursive && (kind == ENTRY_CREATE)) {
					try {
						if (Files.isDirectory(child, NOFOLLOW_LINKS)) {
							registerDeep(child);
						}
					} catch (IOException x) {
						// ignore to keep sample readbale
					}
				}
			}

			// reset key and remove from set if directory no longer accessible
			boolean valid = key.reset();
			if (!valid) {
				keys.remove(key);

				// all directories are inaccessible
				if (keys.isEmpty()) {
					break;
				}
			}
		}
	}
}