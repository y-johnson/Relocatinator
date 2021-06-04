package mediatypes;

import java.util.Calendar;
import java.util.LinkedList;

import static mediatypes.MetadataOps.unwantedBrackets;
import static mediatypes.MetadataOps.unwantedSpaces;

public class Movie extends Media {
	private static final int MOVIE_EARLIEST_YEAR = 1900;
	private static final LinkedList<Integer> MOVIE_VALID_YEARS;

	static {
		MOVIE_VALID_YEARS = new LinkedList<>();

		for (int i = MOVIE_EARLIEST_YEAR; i <= Calendar.getInstance().get(Calendar.YEAR); ++i) {
			MOVIE_VALID_YEARS.add(i);
		}
	}

	private int releaseYear = 0;
	private String movieName;

	/**
	 * A superclass that serves as an intermediary between the File and respective media-related subclasses.
	 * Stores the metadata of the files.
	 *
	 * @param path the path to the Media file
	 */
	public Movie (String path) {
		super(path, MediaTypes.MOVIE);
		extractTitleInfo();
	}

	public int getReleaseYear () {
		return releaseYear;
	}

	public String getMovieName () {
		return movieName;
	}

	private int findReleaseYear () {
		for (Integer i : MOVIE_VALID_YEARS) {
			if (this.getName().contains(i.toString())) {
				return i;
			}
		}

		return releaseYear;
	}

	/**
	 * Extracts relevant information out of the name of the file. This includes the
	 * name of the show, the season the file corresponds to, and the episode number.
	 */
	public void extractTitleInfo () {
		this.movieName = this.getName().substring(0, this.getName().lastIndexOf('.')).trim();

		this.resolution = MetadataOps.getResolution(this.getName());
		this.releaseYear = findReleaseYear();

		for (String e : unwantedSpaces) {
			if (this.movieName.contains(e)) this.movieName = this.movieName.replace(e, " ").trim();
		}

		boolean brackets = true;
		for (int i = 0; i < unwantedBrackets.length - 1; i = i + 2) {
			do {
				if (this.movieName.contains(unwantedBrackets[i]) && this.movieName.contains(unwantedBrackets[i + 1])) {
					String toRemove = this.movieName.substring(this.movieName.indexOf(unwantedBrackets[i]), this.movieName.indexOf(unwantedBrackets[i + 1]) + 1);
					this.movieName = this.movieName.replace(toRemove, "").trim();
				} else {
					brackets = false;
				}
			} while (brackets);
		}
		if (this.releaseYear > MOVIE_EARLIEST_YEAR && this.movieName.contains(Integer.toString(this.releaseYear))) {
			this.movieName = this.movieName.substring(0, this.movieName.indexOf(Integer.toString(this.releaseYear))).trim();
			this.customName = movieName + String.format(" (%4d)", this.releaseYear);
		} else {
			this.customName = movieName;
		}
	}
}
