package media;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MetadataOps {

	protected static final String[] unwantedSpaces = {".", "_"};
	protected static final String[] unwantedBrackets = {"[", "]", "(", ")"};
	protected static final String RES_UNKNOWN = "Unknown";
	protected static final String EPISODE_UNKNOWN = "Special";
	protected static final int MAX_NUMBER_OF_SEASONS = 15;            // Used as a max to find the season number.
	protected static final int MAX_NUMBER_OF_EPISODES = 300;          // Used as a max to find the episode number.
	private static final Logger logger = LoggerFactory.getLogger(MetadataOps.class);
	private static final String RESOLUTION = "Resolution";
	private static final String[] resRegex;

	static {
		resRegex = new String[]{
				"(3996\\s?x\\s?)?(?<" + RESOLUTION + ">2160?)",
				"(1920\\s?x\\s?)?(?<" + RESOLUTION + ">1080p?)",
				"(1280\\s?x\\s?)?(?<" + RESOLUTION + ">720p?)",
				"(854\\s?x\\s?)?(?<" + RESOLUTION + ">480p?)"
		};
	}

	/**
	 * Method designed to verify whether or not a known resolution appears within the given string. The implementation is not currently capable of
	 * parsing data from media; it only does so from their name.
	 * <p>
	 * Future implementations would benefit from ffmpeg's ffprobe functionality. A wrapper for it can be found here:
	 * https://github.com/bramp/ffmpeg-cli-wrapper
	 *
	 * @param fn the name of a file or directory.
	 *
	 * @return the resolution that was found within the given string.
	 */
	public static String getResolution(String fn) {
		logger.debug("Attempting to parse resolution from string \"{}\"", fn);
		for (String resolution : resRegex) {
			logger.trace("Matching \"{}\" against \"{}\".", resolution, fn);
			Pattern p = Pattern.compile(resolution);
			Matcher m = p.matcher(fn);
			if (m.find() && !m.group(RESOLUTION).isBlank()) {
				String trimmedRes = m.group(RESOLUTION).trim();
				logger.debug("Resolution was identified as {} for \"{}\"", trimmedRes, fn);
				return trimmedRes +
						(trimmedRes.endsWith("p") ? "" : "p");
			}
		}

		logger.warn("Resolution was NOT identified for \"{}\"; returning \"{}\".", fn, RES_UNKNOWN);
		return RES_UNKNOWN;
	}

	public static String removeUnwantedSpaces(String s) {
		for (String e : unwantedSpaces) {
			if (s.contains(e)) s = s.replace(e, " ").trim();
		}
		return s;
	}

	static String removeBrackets(String seriesName) {
		if (seriesName == null) return "";
		logger.debug("Removing brackets from string \"{}\".", seriesName);
		for (int bracketArrIdx = 0; bracketArrIdx < unwantedBrackets.length - 1; bracketArrIdx += 2) {
			String leadingBracket = unwantedBrackets[bracketArrIdx];
			String trailingBracket = unwantedBrackets[bracketArrIdx + 1];
			logger.trace("Searching for instances of '{}' within \"{}\".", leadingBracket, seriesName);
			while (seriesName.contains(String.valueOf(leadingBracket))) {
				int idx1 = -1, idx2 = -1;
				char[] snCharArr = seriesName.toCharArray();
				for (int snIdx = 0; (snIdx < snCharArr.length) && !(idx1 < idx2); snIdx++) {                // O(n)
					char c = snCharArr[snIdx];
					if (c == leadingBracket.charAt(0)) {
						logger.trace("Found '{}' within \"{}\" at index {}.", leadingBracket, seriesName, snIdx);
						idx1 = snIdx;
					} else {
						if (c == trailingBracket.charAt(0)) {
							logger.trace("Found '{}' within \"{}\" at index {}.", trailingBracket, seriesName, snIdx);
							idx2 = snIdx;
						}
					}
				}
				if (idx1 < idx2) {
					logger.debug("Removing substring \"{}\" from series name \"{}\".", seriesName.substring(idx1, idx2 + 1), seriesName);
					seriesName = seriesName.replace(seriesName.substring(idx1, idx2 + 1), "");
				} else if (idx1 > -1 && idx2 == -1){
					logger.debug(
							"Leading bracket '{}' does not have a counterpart in string \"{}\", replacing it with ' '.",
							leadingBracket,
							seriesName
					);
					seriesName = seriesName.replace(leadingBracket, " ");
				} else {
					logger.debug(
							"Trailing bracket '{}' does not have a counterpart in string \"{}\", replacing it with ' '.",
							trailingBracket,
							seriesName
					);
					seriesName = seriesName.replace(trailingBracket, " ");
				}
			}
		}
		return seriesName;
	}
}
