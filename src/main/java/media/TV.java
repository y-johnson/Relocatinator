package media;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static media.MetadataOps.unwantedBrackets;
import static media.MetadataOps.unwantedSpaces;

public class TV extends Media {

	private static final Logger logger = LoggerFactory.getLogger(TV.class);
	private static final String SPECIAL = "Special";
	private static final Pattern[] regexSeasonAndEp = {
			Pattern.compile("(?i)(?: *- ?)? ?(?<seasonInd>S(?<season>0*[1-9][0-9]*|0)) *(?<epInd>E(?<episode>0*([1-9][0-9]*|0)))"),
			Pattern.compile(
					"(?i)(?:(?: *- ?)? ?(?<seasonInd>Season ?\\b(?<season>0*([1-9][0-9]*|0))))? *(?: *- ?)? *(?<epInd>Episode ?\\b(?<episode>0*([1-9][0-9]*|0)))"),
			Pattern.compile("(?i)(?: *- ?)? ?(?<seasonInd>(?<season>0*([1-9][0-9]*|0))) *x *(?: *- ?)? *(?<epInd>(?<episode>0*([1-9][0-9]*|0)))")
	};
	/**
	 * "Example TV Show<b> - 01</b>"<br> "Example TV Show<b> 01</b>"
	 */
	private static final Pattern[] regexSoloEp = {
			Pattern.compile(" *- ? *(?<epInd>E?(?<episode>0*([1-9][0-9]*|0)))"),
			Pattern.compile("(?<epInd>E?(?<episode>0*([1-9][0-9]*|0)))$")

	};
	private static final Pattern[] regexSoloSeason = {
			Pattern.compile("(?i)(?<seasonInd>Season ?\\b(?<season>0*([1-9][0-9]*|0)))"),
			Pattern.compile("(?i)(?<seasonInd>S(?<season>0*([1-9][0-9]*|0))) +"),
			};
	protected String seriesName;
	private int seasonNumber = 1;
	private int episodeNumber;

	/**
	 * Creates a TV object, a subclass of Media, that functions the same as a File with added functionality, such as metadata storage and references
	 * to other files.
	 *
	 * @param pathname the path to the Media file
	 */
	public TV(String pathname) {
		super(pathname, MediaType.TV);
		this.extractTitleInfo();
		logger.info("Created \"{}\" ({}) from source file \"{}\"", this.getCustomFilename(), this.getClass().getName(), this.getFile().getName());
	}

	/**
	 * Extracts relevant information out of the name of the file. This includes the name of the show, the season the file corresponds to, and the
	 * episode number.
	 */
	private void extractTitleInfo() {
		// Store resolution from either the non-source directory or from the file's name
		this.resolution = MetadataOps.getResolution(this.getFile().getName());

		parseTVInfo();

		this.seriesName = removeBrackets(this.seriesName);

		if (this.episodeNumber >= 0) {
			this.customName = seriesName.trim() + String.format(" - S%02dE%02d", this.seasonNumber, this.episodeNumber);
		} else {
			this.customName = seriesName.trim() + " - " + SPECIAL;
		}
	}

	private void parseTVInfo() {
		boolean foundEp = false, foundSeas = false;
		logger.info("Parsing TV information for \"{}\"", this.getFile().getName());
		String fn = removeUnwantedSpaces(this.getFile().getName().substring(0, this.getFile().getName().lastIndexOf('.')));
		logger.trace(
				"Proposed file name (before parse) \"{}\" -> \"{}\"",
				this.getFile().getName().substring(0, this.getFile().getName().lastIndexOf('.')),
				fn
		);
		for (Pattern p : regexSeasonAndEp) {
			logger.trace("Matching \"{}\" against \"{}\".", fn, p);
			Matcher matcher = p.matcher(fn);
			if (matcher.find()) {
				logger.debug("Pattern \"{}\" matches against \"{}\".", p, fn);

				// Season
				if (matcher.group("season") != null && matcher.group("seasonInd") != null) {
					logger.trace("Pattern \"{}\" matched \"season\" group with value of {}.", p, Integer.parseInt(matcher.group("season")));
					foundSeas = true;
					this.seasonNumber = Integer.parseInt(matcher.group("season"));
				} else {
					logger.trace("Pattern \"{}\" could NOT match \"season\" group.", p);
					// TODO: put extra season parsing in here, maybe
//					String newFn = matchSeasonOnly(fn);
//					if (!fn.equals(newFn)) {
//
//						fn = newFn;
//						foundSeas = true;
//					} else {
//						// Look in file's immediate parent directory
//						String parentDir = removeUnwantedSpaces(this.file.getParentFile().getName());
//						newFn = matchSeasonOnly(parentDir);
//						if (!parentDir.equals(newFn)) { // If the return value is not equal to the original, the season number was identified
//							// fn stays the same
//							foundSeas = true;
//						}
//					}
				}

				// Episode
				if (matcher.group("episode") != null) {
					logger.trace("Pattern \"{}\" matched \"episode\" group with value of {}.", p, Integer.parseInt(matcher.group("episode")));
					foundEp = true;
					this.episodeNumber = Integer.parseInt(matcher.group("episode"));
				} else {  // If episode was not matched, an exclusive match will be attempted.
					logger.debug("No match for episode # with above pattern; attempting match with exclusive episode regular expressions.");

					String newFn = matchEpisodeOnly(fn);
					if (!fn.equals(newFn)) {
						fn = newFn;
						foundEp = true;
					}
				}

				/* If the "normal" duo regex fully matches, then remove the whole match from the resulting fn */
				if (fn.contains(matcher.group(0))) {
					this.seriesName = fn.replace(matcher.group(0), "").trim();
				} else { // It was likely modified by one of the edge case branches
					this.seriesName = fn;
				}
				logger.debug(
						"{} parse resulted in season # {} and episode # {} with a series name of \"{}\"",
						this.getFile().getName(),
						this.seasonNumber,
						this.episodeNumber,
						this.seriesName
				);
				if (foundSeas && foundEp) {
					return;
				}
			}
		}
		logger.trace("Could not match duo regex against \"{}\",  attempting match with exclusive versions of regular expressions.", fn);
		if (!foundEp) {
			String newFn = matchEpisodeOnly(fn);
			if (!fn.equals(newFn)) {
				fn = newFn;
				foundEp = true;
			}
		}

		if (!foundSeas) {
			String newFn = matchSeasonOnly(fn);
			if (!fn.equals(newFn)) {
				fn = newFn;
				foundSeas = true;
			} else {
				String rawParentDir = this.file.getParentFile().getName();
				logger.debug(
						"Could not parse season information from file's name; parsing parent directory \"{}\" for season information.",
						rawParentDir
				);
				String parentDir = removeUnwantedSpaces(rawParentDir);
				// Look in file's immediate parent directory
				newFn = matchSeasonOnly(parentDir);
				if (!parentDir.equals(newFn)) { // If the return value is not equal to the original, the season number was identified
					// fn stays the same
					foundSeas = true;
				}
			}
		}
		if (!foundEp || !foundSeas) {
			logger.debug("No regular expression pattern match against \"{}\", defaulting to legacy code.", fn);
			this.seriesName = episodes(seasons()).trim();
		} else this.seriesName = fn;
	}

	private static String removeBrackets(String seriesName) {
		if (seriesName == null) return "";
		logger.debug("Removing brackets from string \"{}\".", seriesName);
		for (int bracketArrIdx = 0; bracketArrIdx < unwantedBrackets.length - 1; bracketArrIdx += 2) {
			String leadingBracket = unwantedBrackets[bracketArrIdx];
			logger.trace("Searching for instances of '{}' within \"{}\".", leadingBracket, seriesName);
			while (seriesName.contains(String.valueOf(leadingBracket))) {
				int idx1 = 0, idx2 = 0;
				char[] snCharArr = seriesName.toCharArray();
				for (int snIdx = 0; (snIdx < snCharArr.length) && !(idx1 < idx2); snIdx++) {                // O(n)
					char c = snCharArr[snIdx];
					if (c == leadingBracket.charAt(0)) {
						logger.trace("Found '{}' within \"{}\" at index {}", leadingBracket, seriesName, snIdx);
						idx1 = snIdx;
					} else {
						String trailingBracket = unwantedBrackets[bracketArrIdx + 1];
						if (c == trailingBracket.charAt(0)) {
							logger.trace("Found '{}' within \"{}\" at index {}", trailingBracket, seriesName, snIdx);
							idx2 = snIdx;
						}
					}
				}
				if (idx1 < idx2) {
					logger.debug("Removing substring \"{}\" from series name \"{}\".", seriesName.substring(idx1, idx2 + 1), seriesName);
					seriesName = seriesName.replace(seriesName.substring(idx1, idx2 + 1), "");
				}
			}
		}
		return seriesName;
	}

	private String removeUnwantedSpaces(String s) {
		for (String e : unwantedSpaces) {
			if (s.contains(e)) s = s.replace(e, " ").trim();
		}
		return s;
	}

	private String matchEpisodeOnly(String fn) {
		logger.debug("Parsing episode number from string \"{}\".", fn);
		for (int i = 0; i < regexSoloEp.length; i++) {
			Pattern epPat = regexSoloEp[i];
			Matcher soloEpMatch = epPat.matcher(fn);
			if (soloEpMatch.find() && soloEpMatch.group("episode") != null && soloEpMatch.group("epInd") != null) {
				logger.debug(
						"Exclusive episode pattern (idx = {}) matches against \"{}\" with value of {}.",
						i,
						fn,
						Integer.parseInt(soloEpMatch.group("episode"))
				);
				this.episodeNumber = Integer.parseInt(soloEpMatch.group("episode"));
				fn = fn.replace(soloEpMatch.group(0), "").trim();
				return fn;
			}
		}
		return fn;
	}

	private String matchSeasonOnly(String fn) {
		logger.debug("Parsing season number from string \"{}\".", fn);
		for (int i = 0; i < regexSoloSeason.length; i++) {
			Pattern seasonPat = regexSoloSeason[i];
			Matcher soloSeasonMatcher = seasonPat.matcher(fn);
			if (soloSeasonMatcher.find() && soloSeasonMatcher.group("season") != null && soloSeasonMatcher.group("seasonInd") != null) {
				logger.debug(
						"Exclusive season pattern (idx = {}) matches against \"{}\" with value of {}.",
						i,
						fn,
						Integer.parseInt(soloSeasonMatcher.group("season"))
				);
				this.seasonNumber = Integer.parseInt(soloSeasonMatcher.group("season"));
				fn = fn.replace(soloSeasonMatcher.group(0), "").trim();
				return fn;
			}
		}
		return fn;
	}

	/**
	 * Helper method. Finds the Episode of the current object, if any.
	 *
	 * @return a string of what episode it determined from the file name in '01' notation, "Special" if it could not.
	 */
	private String episodes(String fn) {
		final String[] regex = {
				"(?<seq>Episode (?<num>\\d{1,3}))",
				" ?(?<seq>E(?<num>\\d{1,3})+)",
				"(?<seq>- (?<num>\\d{1,3}))"
		};
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
	 * Helper method. Finds the season of the current TV object, if any. If it is found, returns the name of the file without the season indicator.
	 *
	 * @return the modified name of the file.
	 */
	private String seasons() {
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

	public int getEpisodeNumber() {
		return episodeNumber;
	}

	public String getSeriesName() {
		return seriesName;
	}

	/**
	 * Returns a string representation of this media object's customized file path as a child of the given path. No checks are performed to determine
	 * whether a file already exists at that location.
	 *
	 * @param path path to attach this custom file path to.
	 *
	 * @return the absolute path of this media's customized path structure.
	 */
	@Override
	public Path generateCustomPathStructure(Path path) {
		Path generated = new File(
				path +
						(path.toString().endsWith(File.separator) ? "" : String.valueOf(File.separatorChar)) +
						this.getMediaTitle() +
						File.separatorChar +
						"Season " +
						this.getSeasonNumber() +
						File.separatorChar +
						this.getCustomFilename() +
						this.getExt()
		).toPath();

		logger.trace(
				"Generated path for TV object \"{}\" (given parent path = \"{}\", generated = \"{}\".",
				this.getCustomFilename(),
				path,
				generated
		);

		return generated.toAbsolutePath();
	}

	public int getSeasonNumber() {
		return seasonNumber;
	}

	@Override
	public String getMediaTitle() {
		return this.seriesName;
	}

	@Override
	public boolean isValid() {
		/* If the file exists and both the series name and custom name exist, then it is a valid TV object */
		return this.getFile().isFile() && !this.seriesName.isEmpty() && !this.customName.isEmpty();
	}

}
