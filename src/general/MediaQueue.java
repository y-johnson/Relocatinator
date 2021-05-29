package general;

import yjohnson.PathFinder;
import mediatypes.GenericVideo;
import mediatypes.Media;
import mediatypes.Movie;
import mediatypes.TV;
import yjohnson.ConsoleEvent;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class MediaQueue {

    private static final ArrayList<Media> mediaQueue = new ArrayList<>();

    public static void createMediaQueue() {
        File source, dest;
        String sExt = ".mp4";
        int numFiles = 0;
        do {

            boolean extInS = true;
            boolean doMore = false;

            do {
                // Ask user for source directory
                source = new File(ConsoleEvent.askUserForString("Input the source directory").trim());
                if (!source.isDirectory()) {
                    ConsoleEvent.print("Given string is not a directory or given directory cannot be accessed.", ConsoleEvent.logStatus.ERROR);
                } else {
                    // Ask user for the extension for files found in source
                    do {
                        sExt = ConsoleEvent.askUserForString("Input the file extension used for files in the source folder \n(e.g. '.mp4', '.mkv', or any other extension supported by HandBrake)").trim();
                    } while (!sExt.startsWith("."));

                /*
                Using a File object is completely unnecessary here due to them being used as a toString() method later on
                anyways, but it is the way that the listFiles() returns them. Because listFiles() returns File objects,
                I chose to reduce any additional overheard that comes from turning them into
                 */
                    ArrayList<String> sourceFiles = PathFinder.findFilesAsStrings(source, sExt);

                    if ((numFiles = sourceFiles.size()) == 0) {
                        // No files with extension sExt exists within source
                        ConsoleEvent.print("No files with extension " + sExt + " exists within " + source + ".", ConsoleEvent.logStatus.ERROR);
                        extInS = false;
                    } else {
                        addFromDirToQueue(sourceFiles, sExt, askUserForType(source));
                        doMore = ConsoleEvent.askUserForBoolean("Add additional files to the media queue?");
                    }

                }

            } while (!source.isDirectory() || !extInS || doMore);

            do {
                dest = new File(ConsoleEvent.askUserForString("Input the destination directory"));
                if (!dest.isDirectory())
                    ConsoleEvent.print("Given string is not a directory or given directory cannot be accessed.\n", ConsoleEvent.logStatus.ERROR);
            } while (!dest.isDirectory());

            Main.sourceFolder = source;
            Main.destinationFolder = dest;
            Main.sourceExtension = sExt;
        } while (ConsoleEvent.askUserForBoolean("Source directory: " + source + "\nSource File Extension: " + sExt + "\nDestination Directory: " + dest + "\nFiles to operate on: "+ numFiles +"Confirm?"));
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

    private static void addFromDirToQueue(ArrayList<String> fileStrings, String sourceExt, Media.type typeOfMedia) {
        // Look for files in a specific directory and add them to the queue
        try {
            for (String e : fileStrings) {
                switch (typeOfMedia) {
                    case GENERIC:
                        mediaQueue.add(new GenericVideo(e.toString()));
                        break;
                    case TV:
                        mediaQueue.add(new TV(e.toString()));
                        break;
                    case MOVIE:
                        mediaQueue.add(new Movie(e.toString()));
                        break;
                }
                ConsoleEvent.print("Added " + e + " as " + typeOfMedia + ".", ConsoleEvent.logStatus.DETAIL);
            }
        } catch (IOException io) {
            ConsoleEvent.closeProgram("Program could not access files.", -1);
        }
    }

    /**
     * Counts all the different types of elements in the media queue and returns their value as an array.
     * 0 = TV
     * 1 = MOVIE
     * 2 = GENERIC
     *
     * @return the number of each type found as an array.
     */
    private static int[] count() {
        int[] count = new int[Media.type.values().length];
        for (Media e : mediaQueue) {
            switch (e.getMediaType()) {
                case TV:
                    count[0]++;
                    break;
                case MOVIE:
                    count[1]++;
                    break;
                case GENERIC:
                    count[2]++;
                    break;
                default:
                    try {
                        throw new IllegalStateException("Additional file types have not been implemented for class " + MediaQueue.class.getName());
                    } catch (Exception exception) {
                        exception.printStackTrace();
                    }
                    break;
            }
        }
        return count;
    }

    public static ArrayList<Media> getMediaQueue() {
        return mediaQueue;
    }
}
