package media;

public class MetadataOps {

	protected static final String[] unwantedSpaces = {".", "_"};
	protected static final String[] unwantedBrackets = {"[", "]", "(", ")"};

	private static final String[] res = {
			"1080p",
			"720p",
			"480p",
	};

    static final int MAX_NUMBER_OF_SEASONS = 15;            // Used as a max to find the season number.
    static final int MAX_NUMBER_OF_EPISODES = 300;          // Used as a max to find the episode number.

    /**
	 * Simple method to verify whether or not a known resolution appears within the given string.
	 *
	 * @param fn the name of a file or directory.
	 * @return the resolution that was found within the given string.
	 */
	public static String getResolution (String fn) {
		for (String resolution : res) {
			if (fn.toLowerCase().contains(resolution)) {
				return resolution;
			}
		}
		return "Resolution Unknown";
	}
}
