package general;

import media.Media;
import media.MediaTypes;
import media.Movie;
import media.TV;
import yjohnson.ConsoleEvent;
import yjohnson.PathFinder;

import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

public class MediaList implements Iterable<Media> {
	private String name;
	private Path dir;
	private String ext;
	private final LinkedList<Media> mediaList;

	public MediaList (Path dir, String ext, MediaTypes type) {
		this.mediaList = new LinkedList<>();
		this.dir = dir;
		this.ext = ext;
		HashMap<String, Integer> listNames = new HashMap<>();

		for (File f : PathFinder.findFiles(dir.toFile(), ext)) {
			ConsoleEvent.print(
					"Adding " + f.getName() + " to the list as a " + type.name() + ".",
					ConsoleEvent.logStatus.DETAIL
			);
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
		}

		for (Media m : this.mediaList) {
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
		}

		int largest = -1;
		for (String key : listNames.keySet()) {
			if (listNames.get(key) > largest) {
				largest = listNames.get(key);
				this.name = key;
			}
		}
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
