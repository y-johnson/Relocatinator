package media;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static media.MetadataOps.unwantedBrackets;
import static media.MetadataOps.unwantedSpaces;

public class TV extends Media {

	public static final String SPECIAL = "Special";

	private static final String[] regex = {
			"(?<seq>Episode (?<num>\\d{1,3}))",
			" ?(?<seq>E(?<num>\\d{1,3})+)",
			"(?<seq>- (?<num>\\d{1,3}))"
	};
	private static final Logger logger = LoggerFactory.getLogger(TV.class);
	private static final Pattern[] regexSeasonAndEp = {
			Pattern.compile("(?i)(?: *- ?)? ?(?<seasonInd>S(?<season>0*[1-9][0-9]*|0)) *(?<epInd>E(?<episode>0*([1-9][0-9]*|0)))"),
			Pattern.compile(
					"(?i)(?:(?: *- ?)? ?(?<seasonInd>Season ?\\b(?<season>0*([1-9][0-9]*|0))))? *(?: *- ?)? *(?<epInd>Episode ?\\b(?<episode>0*([1-9][0-9]*|0)))"),
			Pattern.compile("(?i)(?: *- ?)? ?(?<seasonInd>(?<season>0*([1-9][0-9]*|0))) *x *(?: *- ?)? *(?<epInd>(?<episode>0*([1-9][0-9]*|0)))")
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
		super(pathname, MediaType.TV);
		this.extractTitleInfo();
		logger.info("Created \"{}\" ({}) from source file \"{}\"", this.getCustomName(), this.getClass().getName(), this.getFile().getName());
	}

	public String getSeriesName () {
		return seriesName;
	}

	public int getSeasonNumber () {
		return seasonNumber;
	}

	/**
	 * Helper method.
	 * Finds the Episode of the current object, if any.
	 *
	 * @return a string of what episode it determined from the file name in '01' notation, "Special" if it could not.
	 */
	private String episodes (String fn) {
		logger.debug("Parsing episode information for {} object \"{}\".", this.getClass().getName(), this.getFile().getName());
		if (fn.matches(".*\\d{1,3}.*")) { // If the name contains a number between 0 and 999
			String[] templates;
			for (String templatePattern : regex) {
				logger.trace("Matching pattern \"{}\" against \"{}\".", templatePattern, fn);

				var p = Pattern.compile(
						templatePattern,
						Pattern.CASE_INSENSITIVE
				);

				var m = p.matcher(fn);

				if (m.find() && m.groupCount() == 2) {
					this.episodeNumber = Integer.parseInt(m.group("num"));
					logger.debug("Pattern \"{}\" matches against the filename with a result of {}.", templatePattern, this.episodeNumber);
					String seq = m.group("seq");
					return fn.substring(0, fn.indexOf(seq));

				} else {
					logger.trace("Failed matching pattern \"{}\" against \"{}\".", templatePattern, fn);
				}
			}
			logger.debug("Regex pattern matching failed for \"{}\", attempting lazy search.", fn);

			for (int i = 1; i < MetadataOps.MAX_NUMBER_OF_EPISODES; ++i) {
				templates = new String[]{
						String.format("Episode %2d", i),
						String.format("Episode %d", i),
						String.format("- %03d", i),
						String.format("- %02d", i),
						"E" + String.format("%02d ", i),
						("E" + i + " ")
				};

				for (String template : templates) {
					if (fn.toLowerCase().contains(template.toLowerCase())) {
						this.episodeNumber = i;
						logger.debug("Template \"{}\" matches against the filename with a result of {}.", template, this.episodeNumber);
						String substring = fn.substring(0, fn.toLowerCase().indexOf(template.toLowerCase()));

						logger.trace("Returning \"{}\" as reduced filename.", substring);
						return substring;
					}
				}
			}
		}

		logger.warn(
				"No episode information could be retrieved from the filename of \"{}\"; assigning -1 as episode number.",
				this.getFile().getName()
		);
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
		// TODO: This method is inefficient; implementing regex patterns would speed it up and refine it.
		logger.debug("Parsing season information for {} object \"{}\".", this.getClass().getName(), this.getFile().getName());
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

			for (String template : templates) {
				if (fn.contains(template)) {
					this.seasonNumber = i;
					logger.debug("Template \"{}\" matches against the filename with a result of {}.", template, this.seasonNumber);
					return fn.replace(template, "");
				}
			}

		}
		logger.debug("No season information could be retrieved from the filename of \"{}\"; assigning 1 as season number.", this.getFile().getName());
		return fn;
	}

	/**
	 * Extracts relevant information out of the name of the file. This includes the
	 * name of the show, the season the file corresponds to, and the episode number.
	 */
	private void extractTitleInfo () {
		// Store resolution from either the non-source directory or from the file's name
		this.resolution = MetadataOps.getResolution(this.getFile().getName());

		parseTVInfo();
//		this.seriesName = episodes(seasons()).trim();

		boolean brackets = true;
		for (int i = 0; i < unwantedBrackets.length - 1; i = i + 2) {
			do {
				if (this.seriesName.contains(unwantedBrackets[i]) && this.seriesName.contains(unwantedBrackets[i + 1])) {
					String toRemove = this.seriesName.substring(
							this.seriesName.indexOf(unwantedBrackets[i]),
							this.seriesName.indexOf(unwantedBrackets[i + 1]) + 1
					);
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

	private void parseTVInfo () {
		logger.info("Parsing TV information for \"{}\"", this.getFile().getName());
		for (Pattern p : regexSeasonAndEp) {
			String fn = removeUnwantedSpaces(this.getFile().getName().substring(0, this.getFile().getName().lastIndexOf('.')));
			logger.trace("Proposed file name \"{}\" -> \"{}\"", this.getFile().getName().substring(0, this.getFile().getName().lastIndexOf('.')), fn);
			Matcher matcher = p.matcher(fn);
			if (matcher.find()) {
				logger.debug("Pattern \"{}\" matches against \"{}\".", p, fn);
				if (matcher.group("season") != null) this.seasonNumber = Integer.parseInt(matcher.group("season"));
				if (matcher.group("episode") != null) this.episodeNumber = Integer.parseInt(matcher.group("episode"));
				this.seriesName = fn.replace(matcher.group(0), "").trim();
				logger.debug(
						"{} parse resulted in season # {} and episode # {} with a series name of \"{}\"",
						this.getFile().getName(),
						this.seasonNumber,
						this.episodeNumber,
						this.seriesName
				);
				if (matcher.group("seasonInd") != null && matcher.group("epInd") != null && this.seasonNumber >= 0) {
					return;
				} else {
					// TODO: This block should allow for case by case assignments if the previous operations did not fully assign season and episode.
					logger.warn("Not all groups matched, defaulting to legacy code.");
					this.seriesName = episodes(seasons()).trim();
				}
			} else {
				logger.warn("Not all groups matched, defaulting to legacy code.");
				this.seriesName = episodes(seasons()).trim();
			}
		}

	}

	private String removeUnwantedSpaces (String s) {
		for (String e : unwantedSpaces) {
			if (s.contains(e)) s = s.replace(e, " ").trim();
		}
		return s;
	}

	@Override
	public boolean isValid () {
		/* If the file exists and both the series name and custom name exist, then it is a valid TV object */
		return this.getFile().isFile() && !this.seriesName.isEmpty() && !this.customName.isEmpty();
	}
}
