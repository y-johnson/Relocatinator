package mediatypes;

import general.Main;
import yjohnson.ConsoleEvent;

import java.io.*;
import java.util.Date;

public class MediaHistory {

    private static boolean historyEnabled = true; //TODO MAKE USER ASSIGNABLE
    private static BufferedWriter bWriter;

    /**
     * Creates a new historyName file that stores a log of all files that were processed by this program and what it
     * "found out" about the file based off of its name.
     * @param historyName the name of the history file.
     */
    public static void setup (String historyName) {
        if (historyEnabled) {
            ConsoleEvent.print("Creating new history file named " + historyName + "...", ConsoleEvent.logStatus.DETAIL);
            File history = new File(historyName);
            try {
                if (history.createNewFile()) {
                    ConsoleEvent.print("Created " + history.toString() + ".", ConsoleEvent.logStatus.DETAIL);
                } else {
                    ConsoleEvent.print("History file named " + history.toString() + " already exists, overwriting it.", ConsoleEvent.logStatus.NOTICE);
                }

                FileWriter writer = new FileWriter(history);
                bWriter = new BufferedWriter(writer);

            } catch (IOException io) {
                historyEnabled = false;
                ConsoleEvent.print("Could not create or access history file in directory " + Main.PROGRAM_DIR + ".", ConsoleEvent.logStatus.ERROR);
                ConsoleEvent.print(Main.APP_NAME + " will NOT log HandBrake history. Please exit the program and resolve this issue if logging is desired.", ConsoleEvent.logStatus.NOTICE);
                io.printStackTrace();
            }
        }

    }

    /**
     * Adds an entry to the history file that contains the stored metadata in a user-friendly format.
     * @param media the media file to make an entry for.
     */
    public static void addEntry (Media media) {
        if (historyEnabled) {
            try {
                bWriter.write(String.format("\n[%1$tF %1$tT] %2$s\n", new Date(), media.getName()));
                for (String e : appender(media)) {
                    bWriter.write("     " + e + "\n");
                    bWriter.flush();
                }
            } catch (IOException io) {
                historyEnabled = false;
                ConsoleEvent.print("Could not history file in directory " + Main.PROGRAM_DIR + ".", ConsoleEvent.logStatus.ERROR);
                ConsoleEvent.print(Main.APP_NAME + " will NOT log HandBrake history. Please exit the program and resolve this issue if logging is desired.", ConsoleEvent.logStatus.NOTICE);
                io.printStackTrace();
            }
        }

    }

    /**
     * Simple helper method that adds the type-specific description to the media's metadata for use in the history file.
     * @param media the media file to process.
     * @return a string array that contains a user-friendly formatted metadata list for a given media file.
     */
    private static String[] appender(Media media) {
        String[] details = Metadata.mdDesc(media.getMediaType()), content = media.getMetadata();
        String[] result = new String[details.length];
        for (int i = 0; i < details.length; ++i) {
            result[i] = details[i] + ": " + content[i];
        }
        return result;
    }
}
