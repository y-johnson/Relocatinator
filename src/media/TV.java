package media;

import java.util.regex.Pattern;

import static media.MetadataOps.unwantedBrackets;
import static media.MetadataOps.unwantedSpaces;

public class TV extends Media {

	public static final String SPECIAL = "Special";

	private static final String[] regex = {
			"Episode (?<num>\\d{1,3})",
			"S\\d{1,2}[-+]? ?E(?<num>\\d{1,3})+",
			"- (?<num>\\d{1,3})"
	};

	private String seriesName;
	private int seasonNumber = 1, episodeNumber;

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
	 * Helper method.
	 * Finds the Episode of the current object, if any.
	 *
	 * @return a string of what episode it determined from the file name in '01' notation, "Special" if it could not.
	 */
	private String episodes (String fn) {
		String[] templates;
		for (String s : regex) {
			var p = Pattern.compile(
					s,
					Pattern.CASE_INSENSITIVE
			);

			var m = p.matcher(this.file.getName());

			if (m.find()) {
				this.episodeNumber = Integer.parseInt(m.group("num"));
				return fn.substring(0, fn.toLowerCase().indexOf(m.group()));
			}
		}

		for (int i = 1; i < MetadataOps.MAX_NUMBER_OF_EPISODES; ++i) {

			templates = new String[]{
					String.format("Episode %2d", i),
					String.format("Episode %d", i),
					String.format("- %03d", i),
					String.format("- %02d", i),
					"E" + String.format("%02d ", i),
					("E" + i + " ")
			};

			for (String s : templates) {
				if (fn.toLowerCase().contains(s.toLowerCase())) {
					this.episodeNumber = i;
//					return fn.substring(0, fn.lastIndexOf("."));
					return fn.substring(0, fn.toLowerCase().indexOf(s.toLowerCase()));
				}
			}
		}
		this.episodeNumber = -1;
		return fn;
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
		String filename = this.getFile().getName();
		String fn = removeUnwantedSpaces(filename.substring(0, filename.lastIndexOf('.')));
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
					return fn.replace(s, "");
				}
			}

		}
		return fn;
	}

	/**
	 * Extracts relevant information out of the name of the file. This includes the
	 * name of the show, the season the file corresponds to, and the episode number.
	 */
	private void extractTitleInfo () {

		// Store resolution from either the non-source directory or from the file's name
		this.resolution = MetadataOps.getResolution(this.getFile().getName());

		this.seriesName = episodes(seasons()).trim();

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

	private String removeUnwantedSpaces (String s) {
		for (String e : unwantedSpaces) {
			if (s.contains(e)) s = s.replace(e, " ").trim();
		}
		return s;
	}
}
