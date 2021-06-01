package general;

import mediatypes.Media;
import yjohnson.ConsoleEvent;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;

public class MediaQueue {
    private LinkedList<MediaList> queue;

    public MediaQueue() {
        this.queue = new LinkedList<>();
    }

    public static Media.type askUserForType(File dir) {

        ArrayList<String> typeList = new ArrayList<>();
        for (Media.type e : Media.type.values()) {
            typeList.add(e.toString());
        }

        switch (ConsoleEvent.askUserForOption("\nSelect the media type for files in " + dir, typeList)) {
            case 1:
                ConsoleEvent.print("Media type of current operation: " + Media.type.TV, ConsoleEvent.logStatus.NOTICE);
                return Media.type.TV;
            case 2:
                ConsoleEvent.print("Media type of current operation: " + Media.type.MOVIE, ConsoleEvent.logStatus.NOTICE);
                return Media.type.MOVIE;
            case 3:
                ConsoleEvent.print("Media type of current operation: " + Media.type.GENERIC, ConsoleEvent.logStatus.NOTICE);
                return Media.type.GENERIC;
        }

        return Media.type.GENERIC; // Should never reach this point
    }

    public void createMediaQueue() {
        Main.sourceExtension = ".mp4";
        int numFiles = 0;
        do {

            boolean extensionFound = true;
            boolean doMore = false;
            MediaList list;
            do {
                // Ask user for source directory
                Main.sourceFolder = new File(ConsoleEvent.askUserForString("Input the source directory").trim());
                if (!Main.sourceFolder.isDirectory()) {
                    ConsoleEvent.print("Given string is not a directory or given directory cannot be accessed.", ConsoleEvent.logStatus.ERROR);
                } else {
                    do {
                        Main.sourceExtension = ConsoleEvent.askUserForString("Input the file extension used for files in the source folder \n(e.g. '.mp4', '.mkv', or any other extension supported by HandBrake)").trim();
                    } while (!Main.sourceExtension.startsWith("."));

                    do {
                        Main.destinationFolder = new File(ConsoleEvent.askUserForString("Input the destination directory"));
                        if (!Main.destinationFolder.isDirectory())
                            ConsoleEvent.print("Given string is not a directory or given directory cannot be accessed.\n", ConsoleEvent.logStatus.ERROR);
                    } while (!Main.destinationFolder.isDirectory());
                    list = new MediaList(Main.sourceFolder.toPath(), Main.sourceExtension, askUserForType(Main.sourceFolder));

                    if (list.isEmpty()) {
                        // No files with extension sExt exists within source
                        ConsoleEvent.print("No files with extension " + Main.sourceExtension + " exists within " + Main.sourceFolder + ".", ConsoleEvent.logStatus.ERROR);
                        extensionFound = false;
                    } else {
                        queue.add(list);
                        doMore = ConsoleEvent.askUserForBoolean("Add additional files to the media queue?");
                    }
                }
            } while (!Main.sourceFolder.isDirectory() || !extensionFound || doMore);
        } while (ConsoleEvent.askUserForBoolean("Source directory: " + Main.sourceFolder + "\nSource File Extension: " + Main.sourceExtension + "\nDestination Directory: " + dest + "\nFiles to operate on: " + numFiles + "Confirm?"));
    }
}
