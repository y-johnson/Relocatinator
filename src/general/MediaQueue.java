package general;

import filesystem.PathFinder;
import mediatypes.*;
import yjohnson.ConsoleEvent;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class MediaQueue {

    private static final ArrayList<Media> mediaQueue = new ArrayList<>();

    public static void createMediaQueue () {

        /* Ask user for parameters */
        ConsoleEvent.print("ADDING ITEMS TO THE MEDIA QUEUE:");

        File source = new File(Handbrake.DEFAULT_SOURCE_FOLDER), dest = new File(Handbrake.DEFAULT_DESTINATION_FOLDER);
        String sExt = Handbrake.DEFAULT_SOURCE_EXT, dExt = Handbrake.DEFAULT_DESTINATION_EXT;
        boolean extInS = true;
        boolean doMore = false;

        do {
            // Ask user for source directory
            source = new File(ConsoleEvent.askUserForString("Input the source directory").trim());
            if (!source.isDirectory()) ConsoleEvent.print("Given string is not a directory or given directory cannot be accessed.\n", ConsoleEvent.logStatus.ERROR);
            else {
                // Ask user for the extension for files found in source
                do {
                    sExt = ConsoleEvent.askUserForString("Input the file extension used for files in the source folder \n(e.g. '.mp4', '.mkv', or any other extension supported by HandBrake)").trim();
                } while (!sExt.startsWith("."));

                ArrayList<File> sF = PathFinder.findFiles(source, sExt);

                if (sF.size() == 0) {
                    // No files with extension sExt exists within source
                    ConsoleEvent.print("No files with extension " + sExt + " exists within" + source + ".", ConsoleEvent.logStatus.ERROR);
                    extInS = false;

                } else {
                    boolean loop;
                    Media.type t;
                    do {
                        // Ask user for media type
                        t = getType(source);
                        loop = ConsoleEvent.askUserForBoolean("Treat all " + sExt + " files within " + source + " as " + t + "?");
                    } while (!loop);

                    addFromDirToQueue(source, sExt, t);

                    doMore = ConsoleEvent.askUserForBoolean("Add additional files to the media queue?");
                }

            }

        } while (!source.isDirectory() || !extInS || doMore);

        do {
            dest = new File(ConsoleEvent.askUserForString("Input the destination directory"));
            if (!dest.isDirectory()) ConsoleEvent.print("Given string is not a directory or given directory cannot be accessed.\n", ConsoleEvent.logStatus.ERROR);
            else {
                // Ask user for the extension for exported media files
                do {
                    dExt = ConsoleEvent.askUserForString("Input the file extension to convert files into \n(e.g. '.mp4', '.mkv', or any other extension supported by HandBrake)").trim();
                } while (!dExt.startsWith("."));
            }
        } while (!dest.isDirectory());

        Main.sourceFolder = source;
        Main.destinationFolder = dest;
        Main.sourceExtension = sExt;
        Main.destinationExtension = dExt;
//
//        dirAndType.put(source,sExt);
//
//        ConsoleEvent.print("For files with the extension " + sExt + " in " + source + ": ");
//
//
//        ConsoleEvent.print("Creating media queue: " + directory, ConsoleEvent.logStatus.NOTICE);
//        Subtitles.userSubtitleChoice();
//        mediaQueue = new ArrayList<>(addFromDirToQueue(directory));
//
//        int [] numberOf = count();
//        ConsoleEvent.print("Number of files to process: " + mediaQueue.size() + "\n TV: " + numberOf[0] + "\n Movies: " + numberOf [1] + "\n Generic Video: " + numberOf[2]);
//
//        while (ConsoleEvent.askUserForBoolean("Add additional media files to the queue?")) {
//            String tmp = ConsoleEvent.askUserForString("Specify the absolute path to the directory to add from");
//            if (!tmp.endsWith("/")) {
//                tmp += "/";
//            }
//            mediaQueue.addAll(addFromDirToQueue(tmp));
//        }

    }

    public static Media.type getType (File dir) {

        ArrayList<String> typeList = new ArrayList<>();
        for (Media.type e : Media.type.values()) {
            typeList.add(e.toString());
        }

        switch (ConsoleEvent.askUserForOption("Select the media type for files in " + dir,  typeList)) {
            case 1:
                ConsoleEvent.print("Media type of current operation: " + Media.type.TV, ConsoleEvent.logStatus.NOTICE);
                return Media.type.TV;
            case 2:
                ConsoleEvent.print("Media type of current operation: " + Media.type.MOVIE, ConsoleEvent.logStatus.NOTICE);
                return Media.type.MOVIE;
            case 3:
                ConsoleEvent.print("Media type of current operation: " + Media.type.GENERIC, ConsoleEvent.logStatus.NOTICE);
                return Media.type.GENERIC;
            default:
                ConsoleEvent.print("Class " + Media.class.getName() + " does not support additional media types at this moment," +

                        " returning " + Media.type.GENERIC);
        }

        return Media.type.GENERIC; // Should never reach this point
    }

    private static void addFromDirToQueue (File directory, String sourceExt, Media.type typeOfMedia) {

        ConsoleEvent.print("Looking for media files in " + directory, ConsoleEvent.logStatus.DETAIL);

        // Look for files in a specific directory and add them to the queue
        try {
            for (File e : PathFinder.findFiles(directory, sourceExt)) {
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
                ConsoleEvent.print("Added " + mediaQueue.get(mediaQueue.size() - 1).getName() + " as " + mediaQueue.get(mediaQueue.size() - 1).getMediaType().toString() + ".", ConsoleEvent.logStatus.DETAIL);
            }
        } catch (IOException io) {
            ConsoleEvent.closeProgram("Program could not access files.", -1);
        }

        if (!(mediaQueue.size() > 0)) {
            ConsoleEvent.print("No matching media has been found in " + directory + ".", ConsoleEvent.logStatus.NOTICE);
        }

    }

    /**
     * Counts all the different types of elements in the media queue and returns their value as an array.
     *  0 = TV
     *  1 = MOVIE
     *  2 = GENERIC
     * @return the number of each type found as an array.
     */
    private static int[] count () {
        int [] count = new int [Media.type.values().length];
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
