package mediatypes;

import yjohnson.PathFinder;
import general.Main;

import java.io.File;
import java.io.IOException;

public class TV extends Media {

    // PRIVATE VARIABLES
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private static final int MAX_NUMBER_OF_SEASONS = 15;            // Used as a max to find the season number.
    private static final int MAX_NUMBER_OF_EPISODES = 300;          // Used as a max to find the episode number.
    private static String seasonIndicator = "Season Unknown";       // Stores the format that can be used to find the season.
    private static String episodeIndicator = "Episode Unknown";     // Stores the format that can be used to find the episode.
    private static final String defaultSeason = "Season 1";         // Season to default to if none found.

    // CONSTRUCTORS
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Creates a TV object, a subclass of Media, that functions the same as a File with added functionality, such as
     * metadata storage and references to other files.
     * @param pathname the path to the Media file
     * @throws IOException when specified file is not found
     */
    public TV(String pathname) throws IOException {
        /*
         Make a Media object
         Media:
          A superclass that serves as an intermediary between the File and respective media-related subclasses.
          Stores the metadata of the files.
         A.K.A., really important
        */
        super(pathname, type.TV);

        // Sets metadata for parent class
        super.setMetadata(extractTitleInfo(pathname));

        File subs = Subtitles.assignSubs(this);
        if (subs.exists()) {
            super.setSubs(subs.toString());
        }

        // Verify everything is correct
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
     * @param filename the name of the file
     * @return a string array with the following format:
     *  0 = EXTERNAL SUBTITLE LOCATION ("**NO EXT. SUBS**" if none found)
     *  1 = NAME OF SERIES
     *  2 = SEASON NUMBER (NOT_APPLICABLE if not a TV object)
     *  3 = EPISODE NUMBER (NOT_APPLICABLE if not a TV object)
     *  4 = SOURCE LOCATION
     *  5 = DESTINATION LOCATION
     *  6 = CLEAN NAME (name without extra data)
     *  7 = RESOLUTION
     */
    public String [] extractTitleInfo(String filename) throws IOException {

        String [] metadata = {
                "**NO SUBS**",              // Needs to be **NO SUBS** for general.Handbrake to know that it has, well, no subs
                "TV Media File",            // Default, will always be changed
                "Season 1",                 // Default, will be changed as necessary
                "Special",                  // Default, will be changed as necessary
                "Source Unknown",           // Source location
                "Destination Unknown",      // Destination location
                "TV | Full Name",           // Clean name of file
                "Resolution Unknown"        // Resolution of file
        };

        // Store resolution from either the non-source directory or from the file's name
        metadata[7] = Metadata.getResolution(filename.replace(filename.substring(0,filename.lastIndexOf(File.separatorChar)),""));

        // Store source location, then trim filename by removing directory information
        metadata[4] = filename;

        if (filename.contains("/")) {
            filename = filename.substring(filename.lastIndexOf("/")+1);
        } else if (filename.contains("\\")) {
            filename = filename.substring(filename.lastIndexOf("\\")+1);
        }


        // Remove extension from filename
        if (filename.endsWith(Main.sourceExtension)) {
            filename = filename.substring(0,filename.lastIndexOf(Main.sourceExtension));
        }

        // Remove underscores, dots, or other possible "space characters" from filename
        for (String e : unwantedSpaces) {
            if (filename.contains(e)) filename = filename.replaceAll(e," ").trim();
        }

        // Extract release group from filename
        if (filename.startsWith("[")) {
            String rg = filename.substring(filename.indexOf("["),filename.indexOf(']')+1);

            // Update filename without release group
            filename = filename.substring(rg.length());

            // Remove any unwanted spaces
            filename = filename.trim();

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

        // Set clean name
        System.out.println(filename);
        metadata[6] = filename.trim();

        // Find the season number and series name
        metadata[2] = seasons(filename).trim();

        if (filename.contains(seasonIndicator)) {
            metadata[1] = filename.substring(0,filename.indexOf(seasonIndicator)).trim();
        } else if ((metadata[2].equals(defaultSeason))) {
            if (filename.contains(" - ")) {
                metadata[1] = filename.substring(0, filename.indexOf(" -")).trim();
            } else if (filename.contains(" [")) {
                metadata[1] = filename.substring(0, filename.indexOf(" [")).trim();
            } else {
                metadata[1] = filename;
            }
        }

        // Remove all spaces at the end of the name
        filename = filename.trim();

        // Find the episode number
        metadata[3] = episodes(filename);
        if (metadata[1].contains(episodeIndicator) && !episodeIndicator.equals("Special")) {
            metadata[1] = metadata[1].substring(0,metadata[1].indexOf(episodeIndicator)).trim();
        }

        metadata[5] = PathFinder.tvFolders(Main.destinationFolder.toString(), metadata[6], metadata[2], metadata[1]).trim();

        return metadata;
    }

    /**
     * Helper method
     * Finds the Season of the current TV object, if any.
     * @param fn the name of the object
     * @return a string "Season" plus what season it determined from the file name, "Season 1" if it could not.
     */
    private static String seasons (String fn) {
        for (int i = 1; i < MAX_NUMBER_OF_SEASONS; ++i) {
            if (fn.contains("Season " + i) || fn.contains("S" + i) || fn.contains("S0" + i)) {
                //System.out.println("true @ " + i);

                if (fn.contains("Season " + i)) {
                    seasonIndicator = "Season " + i;
                } else if (fn.contains("S" + i)){
                    seasonIndicator = "S" + i;
                } else if (fn.contains("S0" + i)){
                    seasonIndicator = "S0" + i;
                } // If none of these are found, there is no season indicator

                return "Season " + i;
            }
        }

        // If file name does not contain season, assume Season 1
        return  defaultSeason;
    }

    /**
     * Helper method
     * Finds the Episode of the current object, if any.
     * @param fn the name of the object
     * @return a string of what episode it determined from the file name in '01' notation, "Special" if it could not.
     */
    private static String episodes (String fn) {
        for (int i = 1; i < MAX_NUMBER_OF_EPISODES; ++i) {
            /*
            These are the checks for what episode format is being followed by the media file.
             */
            boolean contains1 = fn.contains(String.format("- %02d", i));
            boolean contains2 = fn.contains(String.format("- %03d", i));
            boolean contains3 = fn.contains(("E" + i));
            boolean contains4 = fn.contains("E" + String.format("%02d", i));

            if (contains1 || contains2 || contains3 || contains4) {
                //System.out.println("true @ " + i);

                if (contains1) {
                    episodeIndicator = "- " + String.format("%02d", i);
                } else if (contains2){
                    episodeIndicator = String.format("- %03d", i);
                } else if (contains3) {
                    episodeIndicator = "E" + i;
                } else if (contains4){
                    episodeIndicator = "E" + String.format("%02d", i);
                }

                return String.format("%02d", i);
            }
        }
        /* This point is only reached if the program cannot find the episode by the above method */
        if (fn.contains("-")) return fn.substring(fn.indexOf("-")+1).trim();
        return  "Special";
    }

}
