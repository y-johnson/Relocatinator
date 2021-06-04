package general;

import media.Media;
import media.MediaTypes;
import yjohnson.ConsoleEvent;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

public class MediaQueue implements Iterable<MediaList> {
	private final LinkedList<MediaList> queue;

	public MediaQueue () {
		this.queue = new LinkedList<>();

		File src, dest;
		String ext;

		boolean validSrc, validExt, validDest;

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

		do {
			dest = new File(ConsoleEvent.askUserForString("Input the destination directory"));
			validDest = dest.isAbsolute();
			if (!validDest) ConsoleEvent.print("Invalid directory.", ConsoleEvent.logStatus.ERROR);
		} while (!validDest);

		MediaList subqueue = new MediaList(src.toPath(), ext, askUserForMediaType(src.getAbsolutePath()));
		queue.add(subqueue);
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

	public String stringOfContents () {
		StringBuilder sb = new StringBuilder();
		for (MediaList m : queue) {
			sb.append(m.toString()).append("\n");
			for (Media item : m) {
				sb.append("-").append(item.getCustomName()).append("\n");
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
}
