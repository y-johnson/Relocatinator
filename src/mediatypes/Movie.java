package mediatypes;

import filesystem.PathFinder;
import general.Handbrake;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;


/*
TODO APPROPRIATELY FORMAT MULTI-PART MOVIES SO THAT THEY ALL END UP IN THE SAME FOLDER
 */

public class Movie extends Media {

    // PRIVATE VARIABLES
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private static final int movieBaseYr = 1900;                            // Base year to start search from
    private static final ArrayList<Integer> YR_LIST = new ArrayList<>();    // Every year from movieBaseYr to present day
    public static final String YEAR_UNKNOWN = "Year Unknown";               // If year cannot be found, put this

    // CONSTRUCTORS
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Creates a Movie object, a subclass of Media, that functions the same as a File with added functionality, such as
     * metadata storage and references to other files.
     *
     * @param pathname the path to the Media file
     * @throws IOException when specified file is not found
     */
    public Movie(String pathname) throws IOException {

        super(pathname, type.MOVIE);
        super.setMetadata(extractTitleInfo(pathname));
        for (String e : super.getMetadata()) {
            System.out.println("    " + e);
        }
        MediaHistory.addEntry(this);
    }

    // METHODS
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Extracts relevant information out of the name of the file. This includes the
     * name of the show, the season the file corresponds to, and the episode number.
     *
     * Typical file names:
     *  [%INFO%] %MOVIE_NAME% [%RESOLUTION%][%MORE_INFO%].mkv
     *  %MOVIE_NAME% - %MOVIE_PART_NUMBER% (%RESOLUTION AND MORE_INFO%).mkv
     *
     * @param filename the name of the file
     * @return a string array with the following format:
     *  0 = EXTERNAL SUBTITLE LOCATION ("**NO EXT. SUBS**" if none found)
     *  1 = MOVIE RELEASE YEAR
     *  2 = SEASON NUMBER (NOT_APPLICABLE if not a TV object)
     *  3 = EPISODE NUMBER (NOT_APPLICABLE if not a TV object)
     *  4 = SOURCE LOCATION
     *  5 = DESTINATION LOCATION
     *  6 = CLEAN NAME (name without extra data)
     *  7 = RESOLUTION
     */
    @Override
    public String[] extractTitleInfo(String filename) throws IOException {

        // Get program settings
        String [] metadata = {
                "**NO SUBS**",              // Needs to be **NO SUBS** for general.Handbrake to know that it has, well, no subs
                "Year Unknown",             // Default, will always be changed
                NOT_APPLICABLE,             // Default, will be changed as necessary
                NOT_APPLICABLE,             // Default, will be changed as necessary
                "Source Unknown",           // Source location
                "Destination Unknown",      // Destination location
                "Movie | Full Name",        // Clean name of file
                "Resolution Unknown"        // Resolution of file
        };

        // Store resolution from either the non-source directory or from the file's name
        metadata [7] = Metadata.getResolution(filename.replace(Handbrake.sourceFolder,""));

        // Store source location, then trim filename by removing directory information
        metadata[4] = filename;
        filename = filename.substring(filename.lastIndexOf("/")+1);

        // Remove extension from filename
        if (filename.endsWith(Handbrake.sourceFileExt)) {
            filename = filename.substring(0,filename.lastIndexOf(Handbrake.sourceFileExt));
        }

        // Remove underscores, dots, or other possible "space characters" from filename
        for (String e : unwantedSpaces) {
            boolean uspaceExist = true;
            do {
                if (filename.contains(e)) {
                    filename = filename.replace(e, " ");
                } else {
                    uspaceExist = false;
                }
            } while (uspaceExist);
        }

        // Remove any extra brackets within filename
        boolean brackets = true;

        for (int i = 0; i < unwantedBrackets.length-1; i = i + 2) {
            do {
                if (filename.contains(unwantedBrackets[i]) && filename.contains(unwantedBrackets[i+1])) {
                    String toRemove = filename.substring(filename.indexOf(unwantedBrackets[i]),filename.indexOf(unwantedBrackets[i+1])+1);
                    filename = filename.replace(toRemove, "");
                } else {
                    brackets = false;
                }
            } while (brackets);
        }

        // Find release year within name starting from a base year
        for (int year : YR_LIST) {
            if (filename.contains(Integer.toString(year)) && filename.indexOf(filename.indexOf(Integer.toString(year))+1) != 'p') {
                metadata[1] = Integer.toString(year);
                filename = filename.substring(0,filename.indexOf(metadata[1])).trim();
                break;
            } else {
                metadata[1] = YEAR_UNKNOWN;
            }
        }

        metadata[6] = filename.trim();

        metadata[5] = PathFinder.movieFolders(Handbrake.destinationFolder, metadata[6], metadata[1]);

        return metadata;
    }

    /**
     * Makes a list of all years since movieBaseYr up until the current year.
     */
    public static void setYrList() {
        for (int i = movieBaseYr; i < Calendar.getInstance().get(Calendar.YEAR); ++i) {
            YR_LIST.add(i);
        }
    }

}
