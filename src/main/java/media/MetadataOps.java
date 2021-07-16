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
}
