package media;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yjohnson.PathFinder;

import java.io.File;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

public class MediaQueue implements Iterable<MediaQueue.MediaList> {
	private static final Logger logger = LoggerFactory.getLogger(MediaQueue.class);
	private final LinkedList<MediaList> queue;

	/**
	 * Constructs a LinkedList queue for MediaList objects, which in turn stores Media objects to process together. The resulting MediaQueue object is
	 * iterable at the MediaList level but is meant to be the "accessible" data structure.
	 * <p>
	 * The constructor will use the src and ext parameters to do a recursive file search within the src directory. It will store the paths of all
	 * files that have the given extension.
	 *
	 * @param src  the source directory to search within
	 * @param ext  the extension to filter by
	 * @param type the Media subtype to assign the file to
	 */
	public MediaQueue(Path src, String ext, MediaType type) {
		logger.debug("Creating new media queue.");
		this.queue = new LinkedList<>();

		if (!src.toFile().exists()) {
			logger.error("Given source path (src = \"{}\") does not exist.", src);
			throw new IllegalArgumentException("Given source path (src = \"" + src + "\") does not exist.");
		}

		if (!ext.startsWith(".")) {
			logger.error("Given extension string (ext = \"{}\") for {} constructor is not valid.", ext, this.getClass().getName());
			throw new IllegalArgumentException(
					"Given extension string (ext = \"" + ext + "\") for " + this.getClass().getName() + " constructor is not valid.");
		}

		try {
			if (src.toFile().isDirectory()) {
				switch (type) {
					case MOVIE:
						for (File f : PathFinder.findFiles(src, ext)) {
							MediaList list = new MediaList(f.toPath(), ext, type);
							addMediaListToQueue(list);
						}
						break;
					case TV:
					default:
						MediaList list = new MediaList(src, ext, type);
						addMediaListToQueue(list);
				}
			}


		} catch (NoSuchFileException e) {
			logger.error("Call to MediaList constructor returned a NoSuchFile exception after file was confirmed to exist in MediaQueue constructor.");
			logger.error(e.toString());
			e.printStackTrace();
		}


	}

	private boolean addMediaListToQueue(MediaList list) {
		if (!list.isEmpty()) {
			logger.debug("Media list (name = \"{}\", size = {}) added to queue.", list.name, list.size());
			this.queue.add(list);
			return true;
		} else {
			logger.warn(
					"Media list (\"{}\", \"{}\") is empty; it will not be added to the queue.",
					list.getDirectory(),
					list.getExtension()
			);
			return false;
		}
	}


	@Override
	public Iterator<MediaList> iterator() {
		return queue.iterator();
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Media Queue (sublists = ")
		       .append(this.queue.size())
		       .append(", items = ")
		       .append(this.size())
		       .append("):")
		       .append("\n");

		for (int listIdx = 0; listIdx < queue.size(); listIdx++) {
			MediaList mediaList = queue.get(listIdx);
			builder.append(listIdx + 1)
			       .append(". ")
			       .append(mediaList.toString())
			       .append(" (items = ")
			       .append(mediaList.size())
			       .append(")")
			       .append("\n");

			for (int itemIdx = 0; itemIdx < mediaList.size(); itemIdx++) {
				var item = mediaList.getMediaList().get(itemIdx);
				builder.append("  ")
				       .append(listIdx + 1)
				       .append('.')
				       .append(itemIdx + 1)
				       .append(". ")
				       .append(item.getFile().getName())
				       .append(" -> ")
				       .append(item.getCustomFilename())
				       .append("\n\n");
			}
		}
		return builder.toString();
	}

	public int size() {
		int size = 0;
		for (MediaList l : queue) {
			size += l.size();
		}
		return size;
	}

	public static class MediaList implements Iterable<Media> {
		private final Path dir;
		private final String ext;
		private final LinkedList<Media> mediaList;
		private String name;

		/**
		 * Data structure to be used by MediaQueue in order to allow additional queueing with less complications. When the given path corresponds to a
		 * normal file, creates a MediaList object that contains only it's subsequent media object. Otherwise, retrieves a list of files whose
		 * extension matches the given argument to process them as Media.class subtypes. It will then don the name that corresponds to the majority of
		 * files within the list.
		 *
		 * @param path the directory to search within or the file to use.
		 * @param ext  the extension to filter by.
		 * @param type the type to designate the media with.
		 *
		 * @throws NoSuchFileException when the file or directory does not exist.
		 */
		private MediaList(Path path, String ext, MediaType type) throws NoSuchFileException {
			this.mediaList = new LinkedList<>();
			this.dir = path;
			this.ext = ext;
			File dirF = path.toFile();
			if (!dirF.isDirectory()) {
				if (dirF.isFile() && dirF.canRead()) {
					logger.debug("Given path (path = \"{}\") corresponds to a file; setting this media list as a single-object list.", path);
					addFileToList(type, dirF);
					this.name = this.mediaList.getLast().getMediaTitle();
				} else {
					logger.error("Given path (path = \"{}\") is not recognized as a directory nor as a file.", path);
					logger.warn("Throwing an exception due to invalid path.");
					throw new NoSuchFileException(path.toString());
				}
			} else {
				logger.debug("Creating new media list, type {}; searching for files with extension {} in {}", type.name(), ext, path);
				HashMap<String, Integer> listNames = new HashMap<>();
				for (File f : PathFinder.findFiles(dir, ext)) {
					addFileToList(type, f);

					Media m = this.mediaList.getLast();
					tallyNames(listNames, m);
				}
				int freq = -1;
				for (String key : listNames.keySet()) {
					if (listNames.get(key) > freq) {
						freq = listNames.get(key);
						this.name = key;
					}
				}
				logger.debug("List name has been set to {}. # of files with this name: {}.", this.name, freq);
			}
		}

		private void addFileToList(MediaType type, File f) {
			try {
				logger.trace("Adding {} to the list as a {}.", f.getName(), type.name());
				mediaList.add(type.instantiate(f.toPath()));
			} catch (IllegalArgumentException e) {
				logger.error("Passing file \"{}\" to {} instantiation method produced a \"Not a file\" error", f, type);
				logger.error(e.toString());
				e.printStackTrace();
			}
		}

		/**
		 * Counts the recurrence of a name throughout the files added to this MediaList. The roundup is stored in the given HashMap. Handles new as
		 * well as repeating additions.
		 *
		 * @param listNames hashmap to store results in.
		 * @param m         the media object to tally.
		 */
		private void tallyNames(HashMap<String, Integer> listNames, Media m) {
			if (m.isValid()) {
				var title = m.getMediaTitle();
				if (!listNames.containsKey(title)) {
					logger.trace("Introducing {} (from \"{}\") into list name roundup.", title, m.getCustomFilename());
					listNames.put(title, 1);
				} else {
					logger.trace("Adding +1 to {} (from \"{}\").", title, m.getCustomFilename());
					listNames.replace(title, listNames.get(title) + 1);
				}
			} else {
				logger.error(
						"Generated media file \"{}\" is incomplete or invalid. One of its values may have not been properly processed.",
						m.getCustomFilename()
				);
			}
		}

		private LinkedList<Media> getMediaList() {
			return mediaList;
		}

		@Override
		public String toString() {
			return this.name;
		}

		public Path getDirectory() {
			return dir;
		}

		public String getExtension() {
			return ext;
		}

		public boolean isEmpty() {
			return mediaList.isEmpty();
		}

		public int size() {
			return mediaList.size();
		}

		@Override
		public Iterator<Media> iterator() {
			return this.mediaList.iterator();
		}
	}
}
