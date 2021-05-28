package mediatypes;

public class Metadata {
    private static final String [] res = {
            "1080p",
            "720p",
            "480p",
    };

    /**
     * Simple method to verify whether or not a known resolution appears within the given string.
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

    /**
     * Returns a String array that contains descriptions for the type of media to be processed.
     * Movies and Generic Videos may contain an (N/A) next to their tags. If this is the case, no changes are needed for
     * that entry.
     * To be used for logging purposes.
     * @param type the Media.type
     * @return Returns a String array that contains descriptions for the type of media to be processed.
     */
    protected static String [] mdDesc (Media.type type) {
        switch (type) {
            case MOVIE:
                return new String[]{
                        "External Subtitles",
                        "Movie Release Year",
                        "Season (N/A)",
                        "Episode (N/A)",
                        "File Source Location",
                        "File Output Location",
                        "Movie Name",
                        "Resolution"
                };
            case TV:
                return new String[]{
                        "External Subtitles",
                        "Series' Name",
                        "Season",
                        "Episode",
                        "File Source Location",
                        "File Output Location",
                        "TV Show Name",
                        "Resolution"
                };
            case GENERIC:
                return new String[] {
                        "External Subtitles",
                        "Series' Name / Given Name",
                        "Season (N/A)",
                        "Episode (N/A)",
                        "File Source Location",
                        "File Output Location",
                        "File Name",
                        "Resolution"
                };
            default:         // Will never reach this unless new media types are added
                return new String[] {
                        "", "", "", "", "", "", "UNSUPPORTED MEDIA TYPE", ""
                };
        }
    }
}
