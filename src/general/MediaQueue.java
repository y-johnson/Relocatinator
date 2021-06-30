package general;

import media.Media;
import media.MediaTypes;
import media.Movie;
import media.TV;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yjohnson.ConsoleEvent;
import yjohnson.PathFinder;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

public class MediaQueue implements Iterable<MediaQueue.MediaList> {
	private static final Logger logger = LoggerFactory.getLogger(MediaQueue.class);
	private final LinkedList<MediaList> queue;
	private File destinationDir;

	public MediaQueue () {
		logger.debug("Creating new media queue.");
		this.queue = new LinkedList<>();

		File src;
		String ext;

		boolean validSrc, validExt, validDest;

		do {
			do {
				src = new File(ConsoleEvent.askUserForString("Input the source directory").trim());
				validSrc = src.isDirectory();
				if (!validSrc) ConsoleEvent.print("Invalid directory.", ConsoleEvent.logStatus.ERROR);
			} while (!validSrc);

			do {
				ext = ConsoleEvent.askUserForString("Input the file extension (e.g. '.mkv', '.mp4',...). It must start with a '.'");
				validExt = ext.startsWith(".");
				if (!validExt) ConsoleEvent.print("Invalid extension.", ConsoleEvent.logStatus.ERROR);
			} while (!validExt);

			MediaList subqueue = new MediaList(src.toPath(), ext, askUserForMediaType(src.getAbsolutePath()));
			if (!subqueue.isEmpty()) {
				queue.add(subqueue);
			} else {
				logger.warn(
						"Generated media list (\"{}\", \"{}\") is empty; it will not be added to the queue.",
						subqueue.getDirectory(),
						subqueue.getExtension());
			}
		} while (ConsoleEvent.askUserForBoolean("Add more files to the queue?"));

		do {
			this.destinationDir = new File(ConsoleEvent.askUserForString("Input the destination directory"));
			validDest = this.destinationDir.isAbsolute();
			if (!validDest) ConsoleEvent.print("Invalid directory.", ConsoleEvent.logStatus.ERROR);
		} while (!validDest);


	}

	/**
	 * Helper method to condense constructor of MediaQueue. Uses ConsoleEvent to get user input regarding what type they
	 * want the files to be classified as.
	 *
	 * @param dir directory to be presented to the user.
	 * @return the type of Media the user selected.
	 */
	private static MediaTypes askUserForMediaType (String dir) {
		ArrayList<String> typeList = new ArrayList<>();
		for (MediaTypes e : MediaTypes.values()) {
			typeList.add(e.toString());
		}
		MediaTypes value = MediaTypes.values()[ConsoleEvent.askUserForOption("\nSelect the media type for files in " + dir, typeList) - 1];
		ConsoleEvent.print("Media type of current operation: " + value, ConsoleEvent.logStatus.NOTICE);
		return value;
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
		private MediaList (Path dir, String ext, MediaTypes type) {
			logger.debug("Creating new media list, type {}; searching for files with extension {} in {}", type.name(), ext, dir);
			this.mediaList = new LinkedList<>();
			this.dir = dir;
			this.ext = ext;
			HashMap<String, Integer> listNames = new HashMap<>();

			for (File f : PathFinder.findFiles(dir.toFile(), ext)) {
				logger.trace("Adding {} to the list as a {}.", f.getName(), type.name());
				switch (type) {
					case TV:
						mediaList.add(new TV(f.getAbsolutePath()));
						break;
					case MOVIE:
						mediaList.add(new Movie(f.getAbsolutePath()));
						break;
					default:
						ConsoleEvent.closeProgram("Unhandled type " + type.name() + " was passed onto MediaList.", -1);
				}

				Media m = mediaList.getLast();
				if (m.isValid()) {
					if (m.getType() == MediaTypes.TV) {
						if (!listNames.containsKey(((TV) m).getSeriesName())) {
							listNames.put(((TV) m).getSeriesName(), 1);
						} else {
							listNames.replace(((TV) m).getSeriesName(), listNames.get(((TV) m).getSeriesName()) + 1);
						}
					} else if (m.getType() == MediaTypes.MOVIE) {
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
