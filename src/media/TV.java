package media;

import java.io.File;
import java.io.IOException;

import static media.MetadataOps.unwantedBrackets;
import static media.MetadataOps.unwantedSpaces;

public class TV extends Media {

	public static final String SPECIAL = "Special";
	private String seriesName;
	private int seasonNumber = 1, episodeNumber;

	public String getSeriesName () {
		return seriesName;
	}

	public int getSeasonNumber () {
		return seasonNumber;
	}

	public int getEpisodeNumber () {
		return episodeNumber;
	}

	/**
	 * Creates a TV object, a subclass of Media, that functions the same as a File with added functionality, such as
	 * metadata storage and references to other files.
	 *
	 * @param pathname the path to the Media file
	 */
	public TV (String pathname) {
		super(pathname, MediaTypes.TV);
		this.extractTitleInfo();
	}

	/**
	 * Helper method.
	 * Finds the Episode of the current object, if any.
	 *
	 * @return a string of what episode it determined from the file name in '01' notation, "Special" if it could not.
	 */
	private String episodes (String fn) {
		String[] templates;
		for (int i = 1; i < MetadataOps.MAX_NUMBER_OF_EPISODES; ++i) {
			templates = new String[]{
					String.format("- %02d", i),
					String.format("- %03d", i),
					("E" + i),
					"E" + String.format("%02d", i)
			};

			for (String s : templates) {
				if (fn.contains(s)) {
					this.episodeNumber = i;
					return fn.substring(0, fn.indexOf(s));
				}
			}
		}

		this.episodeNumber = -1;
		return fn.substring(0, fn.lastIndexOf("."));
	}

	/**
	 * Helper method.
	 * Finds the season of the current TV object, if any. If it is found, returns the name of the file without the season
	 * indicator.
	 *
	 * @return the modified name of the file.
	 */
	private String seasons () {
		String[] templates;
		String fn = this.getFile().getName();
		for (int i = 1; i < MetadataOps.MAX_NUMBER_OF_SEASONS; ++i) {
			templates = new String[]{
					"SEASON " + i,
					"Season " + i,
					"season " + i,
					"s" + i,
					"s0" + i,
					"S" + i,
					"S0" + i
			};

			for (String s : templates) {
				if (fn.contains(s)) {
					this.seasonNumber = i;
					return this.getFile().getName().replace(s, "");
				}
			}

		}
		return this.getFile().getName();
	}

	/**
	 * Extracts relevant information out of the name of the file. This includes the
	 * name of the show, the season the file corresponds to, and the episode number.
	 */
	private void extractTitleInfo () {

		// Store resolution from either the non-source directory or from the file's name
		this.resolution = MetadataOps.getResolution(this.getFile().getName());

		this.seriesName = episodes(seasons()).trim();

		for (String e : unwantedSpaces) {
			if (this.seriesName.contains(e)) this.seriesName = this.seriesName.replace(e, " ").trim();
		}

		boolean brackets = true;
		for (int i = 0; i < unwantedBrackets.length - 1; i = i + 2) {
			do {
				if (this.seriesName.contains(unwantedBrackets[i]) && this.seriesName.contains(unwantedBrackets[i + 1])) {
					String toRemove = this.seriesName.substring(this.seriesName.indexOf(unwantedBrackets[i]), this.seriesName.indexOf(unwantedBrackets[i + 1]) + 1);
					this.seriesName = this.seriesName.replace(toRemove, "").trim();
				} else {
					brackets = false;
				}
			} while (brackets);
		}

		if (this.episodeNumber >= 0) {
			this.customName = seriesName.trim() + String.format(" - S%02dE%02d", this.seasonNumber, this.episodeNumber);
		} else {
			this.customName = seriesName.trim() + " - " + SPECIAL;
		}

	}
}
