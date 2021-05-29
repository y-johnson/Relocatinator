package mediatypes;

import yjohnson.PathFinder;
import general.Main;
import yjohnson.ConsoleEvent;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

public class Subtitles {

    public static void setSubType(SubtitleType subType) {
        Subtitles.subType = subType;
    }

    /**
     * Stores the user's selected subtitle option from SubtitleType.
     * All subtitle files under the source folder directory will be searched if EXTSUBS is selected.
     */
    private static SubtitleType subType;
    private static boolean subSearchEnabled = false;
    private static final ArrayList<File> allSubs = new ArrayList<>();

    /**
     * Subtitle track for HandBrakeCLI. By default, it is set to -1.
     */
    public static int subTrack = -1;

    /**
     * Enum used to keep track of what subtitle choice the user decided to enforce.
     *  NOSUBS = No subtitles will be searched for or applied to the final media file.
     *  EXTSUBS = The program will search for external subtitles that match and burn them to the final media file.
     *  INTSUBS = The program will not search for subtitles and will burn the subtitles found in the media file.
     */
    public enum SubtitleType {
        NOSUBS, EXTSUBS, INTSUBS
    }

    /**
     * Extensions for subtitle files to be used in HandBrakeCLI's command line arguments. These are the current formats
     * supported by the program, according to the documentation.
     */
    private static final String [] subsExtensions = {
            ".srt",
            ".ssa",
            ".ass"
    };
    /**
     * Prompts the user to define their choices for subtitles, such as whether or not they want to exclude subs from the
     * conversion or if they would want to use internal subs instead.
     * NOTE: If either option is selected and cannot be found by either this program or general.Handbrake CLI, this will fail
     * and function exactly as if no subtitles were chosen.
     */
    public static void userSubtitleChoice () {
        if (ConsoleEvent.askUserForBoolean("Subtitles")) {
            String x =
                    "All subtitle files within this directory will be searched and paired to a media file" +
                    "as long as they are named similarly. \nAlternatively, it can only use the subtitle files that are part " +
                    "of the media file. \nIn both cases, they will be burned into the resulting file.";
            switch (ConsoleEvent.askUserForOption(x, new ArrayList<>(Arrays.asList("External Subtitles", "Internal Subtitles")))) {
                case 1:
                    subType = SubtitleType.EXTSUBS;
                    subSearchEnabled = true;
                    break;
                case 2:
                    subType = SubtitleType.INTSUBS;
                    subTrack = ConsoleEvent.askUserForInt(
                            "Specify the subtitle track number. " +
                            "You can verify which one it is in VLC. If unsure, type 1 for the default subtitle track."
                    );
                    break;
            }
        } else {
            ConsoleEvent.print("Subtitle Search disabled.");
            subType = SubtitleType.NOSUBS;
        }

        findAllSubs(subSearchEnabled);
    }

    public static final File
            SUB_SEARCH_DISABLED = new File("SubSearchDisabled"),
            SUB_NOT_FOUND = new File("FileNotFound")
                    ;

    /**
     * Finds all files that contain a supported extension for subtitles and attempts to narrow down which subs
     * corresponds to the Media file by looking for the one with the most similar name, if none was provided by the user.
     *
     * It will only assign subs that follow the exact same name format as the original Media file. The Media file
     *  Phineas and Ferb - S1E14.mkv
     *  Elf (2003).mkv
     * will *only* assign a sub file that is formatted as:
     *  Phineas and Ferb - S1E14 [ WORKS ]
     *  Elf (2003) [ WORKS ]
     * and not:
     *  Phineas and Ferb Season 1 - 14 [ FAILS ]
     *  Elf (Will Ferrell) 2003 [ FAILS ]
     *
     * @param media the media file whose subtitles the program will look for
     * @return the File of the subtitles
     */
    public static File assignSubs (Media media) {
        if (!subSearchEnabled) {
            return SUB_SEARCH_DISABLED;
        }

        String[] md = media.getMetadata();

        for (File s : allSubs) {
            // Reduces it to just the file's name
            String subName = s.toString().substring(s.toString().indexOf("/"));

            if (subName.contains(md[6])) {              // Looks for clean file name inside the subtitle name
                ConsoleEvent.print("Compatible external subtitles found for " + md[6] + " located at " + s);
                return s;
            }
        }
        if (allSubs.size() != 0) ConsoleEvent.print("No compatible external subtitles found for " + md[6]);
        return SUB_NOT_FOUND;
    }

    /**
     * Recursively finds all subtitle files in the source directory for future reference
     */
    public static void findAllSubs(boolean subSearchEnabled) {
        if (!subSearchEnabled) return;

        // Looks for all subs in all subdirectories with findFiles
        ConsoleEvent.print("Searching for all subtitle files in " + Main.sourceFolder, ConsoleEvent.logStatus.NOTICE);
        for (String e : subsExtensions) {
            allSubs.addAll(PathFinder.findFiles(Main.sourceFolder, e));
        }
        for (File e : allSubs) {
            ConsoleEvent.print("Found " + e.toString(), ConsoleEvent.logStatus.DETAIL);
        }
    }

    /**
     * Gets the user's chosen subtitle option as chosen in userSubtitleChoice.
     * @return user's chosen subtitle option.
     */
    public static SubtitleType getSubType() {
        return subType;
    }
}
