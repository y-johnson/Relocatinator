package general;

import yjohnson.PathFinder;
import mediatypes.Media;
import mediatypes.Movie;
import mediatypes.Subtitles;
import yjohnson.ConsoleEvent;
import yjohnson.TextFinder;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;

import static general.Main.PROGRAM_DIR;

/**
 * TODO HANDBRAKE WILL ONLY OUTPUT TO WHATEVER DIRECTORY IT IS IN, USE { cd &OUTPUT_DIRECTORY&} TO GET THERE AND USE A RELATIVE PATH FOR THE OUTPUT FILE
 */

public class Handbrake {

    /* Located where the program is */
    public static final String
            DEFAULT_HANDBRAKE_LOCATION = PROGRAM_DIR + "HandBrakeCLI.exe",
            DEFAULT_SOURCE_FOLDER = PROGRAM_DIR + "Source/", DEFAULT_SOURCE_EXT = ".mkv",
            DEFAULT_DESTINATION_FOLDER = PROGRAM_DIR + "Destination/", DEFAULT_DESTINATION_EXT = ".mp4";
    static final Properties prop = new Properties();


    // PROPERTIES FOR THE PROGRAM
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private static final ArrayList<String> addArgs = new ArrayList<>();
    public static String sourceFolder, sourceFileExt;
    public static String destinationFolder, destinationFileExt;
    // Additional parameters toggle and array
    private static boolean additionalArgs = false;
    private static String handbrakeLocation;
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public static boolean isPresent(File hbLocation) {
        File hb_check = new File("hb_check.txt");
        try {
            if (hb_check.exists()) {
                if (hb_check.delete()) {
                    if (!hb_check.createNewFile())
                        throw new IOException("Method isPresent() failed; could not create file \"hb_check.txt\" to validate HandBrake.");
                } else {
                    throw new IOException("Method isPresent() failed; could not delete pre-existing file \"hb_check.txt\" to validate HandBrake.");
                }
            } else {
                if (!hb_check.createNewFile())
                    throw new IOException("Method isPresent() failed; could not create file \"hb_check.txt\" to validate HandBrake.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // This is where the fun begins.
        ConsoleEvent.print("Initializing ProcessBuilder with the following command:\n " + hbLocation + "\n", ConsoleEvent.logStatus.DETAIL);
        ProcessBuilder pb = new ProcessBuilder().command(hbLocation.toString());

        try {
            final Process p = pb.redirectOutput(hb_check).redirectError(hb_check).start();
            p.waitFor();
            ConsoleEvent.print("\nCreated " + hb_check + ".", ConsoleEvent.logStatus.DETAIL);

            boolean success = TextFinder.isStringInFile("HandBrake has exited.", hb_check, false);
            if (!hb_check.delete()) {   // Ideally it deletes it here, but it will not crash if it cannot.
                ConsoleEvent.print("Method isPresent() could not delete newly created file \"hb_check.txt\" to validate HandBrake.", ConsoleEvent.logStatus.ERROR);
            }
            return success;

        } catch (IOException | InterruptedException e) {
            ConsoleEvent.print(Main.APP_NAME + " could not execute a terminal command properly. This could be due to permissions, invalid parameters, or a working directory problem.", ConsoleEvent.logStatus.ERROR);
            ConsoleEvent.closeProgram(Arrays.toString(e.getStackTrace()), -1);
        }

        if (!hb_check.delete()) {   // Ideally it deletes it here, but it will not crash if it cannot.
            ConsoleEvent.print("Method isPresent() could not delete newly created file \"hb_check.txt\" to validate HandBrake.", ConsoleEvent.logStatus.ERROR);
        }
        return false;
    }

    /**
     * Allows the user to input their own options for HandbrakeCLI in an argument-by-argument basis.
     */
    public static void setAdditionalArgs() {

        if (ConsoleEvent.askUserForBoolean("Do you want to set additional arguments for HandbrakeCLI?")) {
            additionalArgs = true;
            addArgs.addAll(ConsoleEvent.askUserForArguments("Type all additional arguments for HandbrakeCLI in order."));

            ConsoleEvent.print("Additional Arguments: " + addArgs.toString());

            if (!ConsoleEvent.askUserForBoolean("Confirm arguments?")) {
                addArgs.clear();
                setAdditionalArgs();
            }
        } else {
            ConsoleEvent.print("No additional arguments have been set for HandBrakeCLI");
        }

    }

    /**
     * Opens an instance of HandBrakeCLI, the command line version of HandBrake, with the arguments needed to
     * find a file, prepare the program, and convert the file to another format. If available, the output file
     * will use the metadata that is relevant to it for the filename.
     *
     * @param media the file's metadata
     */
    public static void processMediaFile(Media media) {

        Instant start = Instant.now();
        media.setCurrentStatus(Media.status.PROCESSING);
        // Metadata for the current media file
        String[] md = media.getMetadata();
        String destMediaName = md[6];       // Will be changed, makes compiler happy

        // Sets up the media's file name depending on the media type
        switch (media.getMediaType()) {
            case TV:
                // Name Format: %NAME_OF_SHOW% (%SEASON_NUMBER%) - %EP_NUMBER%.%DEST_FILE_EXT%
                destMediaName = md[1] + " (" + md[2] + ") - " + md[3] + destinationFileExt;
                break;
            case MOVIE:
                // Name Format: %NAME_OF_MOVIE% (%YEAR%).%DEST_FILE_EXT%
                destMediaName = md[6];
                if (!md[1].equals(Movie.YEAR_UNKNOWN)) destMediaName += " (" + md[1] + ")";
                destMediaName += destinationFileExt;
                break;
            case GENERIC:
                // Name Format: %NAME_OF_FILE% (%YEAR%).%DEST_FILE_EXT%
                destMediaName = md[6] + destinationFileExt;  // TODO
                break;
        }

        // Establishes the command line arguments that HandbrakeCLI will use to convert the files
        ArrayList<String> handbrakeArgs = new ArrayList<>();

        /*
        Sets CMD to the output folder before doing anything else
         */
//        handbrakeArgs.add("cd"); handbrakeArgs.add("\"" +md[5] + "\""); handbrakeArgs.add("&&");
        handbrakeArgs.add("\"" + handbrakeLocation + "\"");

        // Options
        if (additionalArgs) {
            handbrakeArgs.addAll(addArgs);
        }

        // NOTE: There is no need to have so many .add statements, however in this case it is for clarity.
        if (Subtitles.getSubType().equals(Subtitles.SubtitleType.INTSUBS)) {
            // Selects first subtitle track and burns it into the video
            handbrakeArgs.add("--subtitle");
            handbrakeArgs.add("" + Subtitles.subTrack);      // Default is -1 (anything goes)
            handbrakeArgs.add("--subtitle-burn");          // Burns it in
        } else if (Subtitles.getSubType().equals(Subtitles.SubtitleType.EXTSUBS) && !md[0].equals("**NO SUBS**")) {
            String sub, burn;
            if (md[0].endsWith(".ssa") || md[0].endsWith(".ass")) {
                sub = "--ssa-file";
                burn = "--ssa-burn";
            } else if (md[0].endsWith(".srt")) {
                sub = "--srt-file";
                burn = "--srt-burn";
            } else {
                ConsoleEvent.print("Incompatible subtitle file, ignoring user subtitle choice.", ConsoleEvent.logStatus.NOTICE);
                sub = "";
                burn = "";
            }
            handbrakeArgs.add(sub);        // Subtitle type
            handbrakeArgs.add(md[0]);      // Location of subtitle
            handbrakeArgs.add(burn);       // Burn-in command. HandBrake needs a specific command for each subtitle.
        }

        handbrakeArgs.add("-i");                   // Input
        handbrakeArgs.add("\"" + md[4] + "\"");                  // File source location
        handbrakeArgs.add("-o");                   // Output
        handbrakeArgs.add("\"" + destMediaName.trim() + "\"");  // File output location


        // This sets up a way to log operation on a per-file basis
        // First, it must make sure it can write to a folder such as HandBrake logs
        String path = PROGRAM_DIR + "\\HandBrake Logs\\";
        PathFinder.verifyExistence(path, true);

        // Then, it prepares a file to write to inside of path
        File hbLog = new File(path + media.getName() + ".txt");
        try {
            ConsoleEvent.print(hbLog.createNewFile() ?
                    (hbLog + " created.") : ("Overwriting " + hbLog + "."), ConsoleEvent.logStatus.DETAIL);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // This is where the fun begins.
        ConsoleEvent.print("Initializing ProcessBuilder with the following command:\n   " + handbrakeArgs.toString());
        ProcessBuilder pb = new ProcessBuilder().command(handbrakeArgs);
        pb.directory(new File(md[5] + "\\"));

        try {
            ConsoleEvent.print("Processing " + destMediaName + "...");
            final Process p = pb.redirectOutput(hbLog).redirectError(hbLog).start();
            p.waitFor();
            ConsoleEvent.print("\nProcessed " + destMediaName + ".");

            media.setCurrentStatus(handbrakeCompletionStatus(media, hbLog));

        } catch (IOException | InterruptedException e) {
            ConsoleEvent.print(Main.APP_NAME + " could not execute the command properly. This could be due to permissions, invalid parameters, or a working directory problem.", ConsoleEvent.logStatus.ERROR);
            ConsoleEvent.closeProgram(Arrays.toString(e.getStackTrace()), -4);
        }

        Instant end = Instant.now();
        media.setTimeElapsedWhileProcessing(Duration.between(start, end));
    }

    private static Media.status handbrakeCompletionStatus(Media media, File log) {
        if (TextFinder.isStringInFile("Encode done!", log, false)) {
            ConsoleEvent.print("HandBrake reports successful encode for " + media.getName());
            return Media.status.PROCESSED;
        } else if (TextFinder.isStringInFile("Encode failed", log, false)) {
            ConsoleEvent.print("Permissions are not properly set up for HandBrake to access the destination folder for " + media.getName(), ConsoleEvent.logStatus.ERROR);
            return Media.status.PERMISSIONS_ERROR;
        } else {
            ConsoleEvent.print("HandBrake has reported something other than a successful encode for " + media.getName() + ", verify output.", ConsoleEvent.logStatus.ERROR);
            return Media.status.ERROR;
        }
    }
}
