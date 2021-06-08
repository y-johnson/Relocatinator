package media;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MetadataOps {

	protected static final String[] unwantedSpaces = {".", "_"};
	protected static final String[] unwantedBrackets = {"[", "]", "(", ")"};
	static final int MAX_NUMBER_OF_SEASONS = 15;            // Used as a max to find the season number.
	static final int MAX_NUMBER_OF_EPISODES = 300;          // Used as a max to find the episode number.

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
	 * Simple method to verify whether or not a known resolution appears within the given string.
	 *
	 * @param fn the name of a file or directory.
	 * @return the resolution that was found within the given string.
	 */
	public static String getResolution (String fn) {
		for (String resolution : resRegex) {
			Pattern p = Pattern.compile(resolution);
			Matcher m = p.matcher(fn);
			if (m.find() && !m.group(RESOLUTION).isBlank()) {
				return m.group(RESOLUTION).trim() +
						(m.group(RESOLUTION).trim().endsWith("p") ? "" : "p");
			}
		}
		return "Resolution Unknown";
	}
}
