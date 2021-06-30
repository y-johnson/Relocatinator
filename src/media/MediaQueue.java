package media;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yjohnson.PathFinder;

import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

public class MediaQueue implements Iterable<MediaQueue.MediaList> {
	private static final Logger logger = LoggerFactory.getLogger(MediaQueue.class);
	private final LinkedList<MediaList> queue;
	private File destinationDir;

	/**
	 * Constructs a LinkedList queue for MediaList objects, which in turn stores Media objects to process together. The resulting MediaQueue object is
	 * iterable at the MediaList level but is meant to be the "accessible" data structure.
	 *
	 * The constructor will use the src and ext parameters to do a recursive file search within the src directory. It will store the paths of all files
	 * that have the given extension.
	 *
	 * @param src the source directory to search within
	 * @param ext the extension to filter by
	 * @param target the destination directory
	 * @param type the Media subtype to assign the file to
	 */
	public MediaQueue (Path src, String ext, Path target, MediaType type) {
		logger.debug("Creating new media queue.");
		boolean validSrc = src.toFile().isDirectory();
		boolean validExt = ext.startsWith(".");
		boolean validTarget = target.isAbsolute();

		if (!validSrc) {
			if (!src.toFile().exists()) throw new IllegalArgumentException(
					"Given source path (src = \"" + src + "\") does not exist.");
			else if (src.toFile().isFile()) throw new IllegalArgumentException(
					"Given source path (src = \"" + src + "\") is a file; directory expected.");
			else throw new IllegalArgumentException(
						"Given source path (src = \"" + src + "\") for " + this.getClass().getName() + " constructor is not valid.");
		}
		if (!validExt) {
			throw new IllegalArgumentException(
					"Given extension string (ext = \"" + ext + "\") for " + this.getClass().getName() + " constructor is not valid.");
		}
		if (!validTarget) {
			throw new IllegalArgumentException(
					"Given target path (target = \"" + target + "\") for " + this.getClass().getName() + " constructor is not valid.");
		}

		destinationDir = target.toFile();
		this.queue = new LinkedList<>();

		MediaList list = new MediaList(src, ext, type);
		if (!list.isEmpty()) {
			logger.debug("Media list (name = \"{}\", size = {}) added to queue.", list.name, list.size());
			queue.add(list);
		} else {
			logger.warn(
					"Generated media list (\"{}\", \"{}\") is empty; it will not be added to the queue.",
					list.getDirectory(),
					list.getExtension()
			);
		}

	}

	public File getDestinationDir () {
		return destinationDir;
	}

	public String stringOfContents () {
		StringBuilder sb = new StringBuilder();
		for (MediaList m : queue) {
			sb.append(m.toString()).append("\n");
			for (Media item : m) {
				sb.append(item.getFile().getName()).append("\n");
				sb.append(" -> ").append(item.getCustomName()).append("\n");
			}
		}
		return sb.toString();
	}

	public int size () {
		int size = 0;
		for (MediaList l : queue) {
			size += l.size();
		}
		return size;
	}

	@Override
	public Iterator<MediaList> iterator () {
		return queue.iterator();
	}

	public static class MediaList implements Iterable<Media> {
		private static final Logger logger = LoggerFactory.getLogger(MediaList.class);
		private final Path dir;
		private final String ext;
		private final LinkedList<Media> mediaList;
		private String name;

		/**
		 * Data structure to be used by MediaQueue in order to allow additional queueing with less complications.
		 * Retrieves a list of files whose extension matches the given argument to process them as Media.class subtypes.
		 * It will then don the name that corresponds to the majority of files within the list.
		 *
		 * @param dir  the directory to search within.
		 * @param ext  the extension to filter by.
		 * @param type the type to designate the media with.
		 */
		private MediaList (Path dir, String ext, MediaType type) {
			logger.debug("Creating new media list, type {}; searching for files with extension {} in {}", type.name(), ext, dir);
			this.mediaList = new LinkedList<>();
			this.dir = dir;
			this.ext = ext;
			HashMap<String, Integer> listNames = new HashMap<>();

			for (File f : PathFinder.findFiles(dir.toFile(), ext)) {
				logger.trace("Adding {} to the list as a {}.", f.getName(), type.name());
				try {
					mediaList.add(type.instantiate(f.toPath()));
				} catch (IllegalArgumentException e) {
					logger.error("Passing file \"{}\" to {} instantiation method produced a \"Not a file\" error", f, type);
				}

				Media m = mediaList.getLast();
				if (m.isValid()) {
					if (m.getType() == MediaType.TV) {
						if (!listNames.containsKey(((TV) m).getSeriesName())) {
							listNames.put(((TV) m).getSeriesName(), 1);
						} else {
							listNames.replace(((TV) m).getSeriesName(), listNames.get(((TV) m).getSeriesName()) + 1);
						}
					} else if (m.getType() == MediaType.MOVIE) {
						if (!listNames.containsKey(((Movie) m).getMovieName())) {
							listNames.put(((Movie) m).getMovieName(), 1);
						} else {
							listNames.replace(((Movie) m).getMovieName(), listNames.get(((Movie) m).getMovieName()) + 1);
						}
					}
				} else {
					logger.error(
							"Generated media file \"{}\" is incomplete or invalid. One of its values may have not been properly processed.",
							m.getCustomName()
					);
				}
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

		@Override
		public String toString () {
			return this.name;
		}

		public Path getDirectory () {
			return dir;
		}

		public String getExtension () {
			return ext;
		}

		public boolean isEmpty () {
			return mediaList.isEmpty();
		}

		public int size () {
			return mediaList.size();
		}

		@Override
		public Iterator<Media> iterator () {
			return this.mediaList.iterator();
		}
	}
}
