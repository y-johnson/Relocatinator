package general;

import mediatypes.Media;
import mediatypes.MediaHistory;
import yjohnson.ConsoleEvent;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Main {

    public static String APP_NAME;
    public static String VERSION;
    public static File PROGRAM_DIR;
    public static Date CURR_DATE;

    // Paths to the source and destination folders
    public static File sourceFolder, destinationFolder, handbrakeLocation;
    public static String sourceExtension, destinationExtension;


    static {
        APP_NAME = "Relocatinator";
        VERSION = "0.2";
        PROGRAM_DIR = new File(System.getProperty("user.dir"));
        CURR_DATE = new Date();
    }

    /**
     * Main program setup.
     */
    private static void setup() {
        /* Intro for user */
        StringBuilder sb = new StringBuilder();
        sb.append('-').append(APP_NAME.toUpperCase()).append(" v").append(VERSION).append('-').append("\n");
        ConsoleEvent.print(sb.toString());

        /* Ask user if they want to see all events or just the ones that concern them; everything will be logged regardless. */
        sb = new StringBuilder();
        sb.append(APP_NAME).append(" will generate a log file called \"log.txt\" in the program's current operating directory.\n");
        ConsoleEvent.setShowUserAllEvents(ConsoleEvent.askUserForBoolean("Show debug text?"));
        ConsoleEvent.print(sb.toString());

        boolean correct = false;
        /* Input all relevant directories */
        // HandBrake
        do {
            File h;
            do  {
                h = new File(ConsoleEvent.askUserForString("Location for HandBrake (CLI version): "));
            } while (!h.exists() || !Handbrake.isPresent(h));
            handbrakeLocation = h;

            sb = new StringBuilder();
            sb.append("HandBrake location: ").append(handbrakeLocation).append("\n");
            ConsoleEvent.print(sb.toString());

            correct = ConsoleEvent.askUserForBoolean("Confirm?");
        } while (!correct);

        /* Verifies that a file named HandbrakeCLI.exe is present in the root folder */

        SimpleDateFormat formatter = new SimpleDateFormat(" [dd-MM-yyyy][HH-mm-ss]");
        MediaHistory.setup("History" + formatter.format(CURR_DATE) + ".txt");

        MediaQueue.createMediaQueue();

        Handbrake.setAdditionalArgs();

    }


    public static void main(String[] args) {

        setup();

        // Capable of having multiple media types
        for (Media file : MediaQueue.getMediaQueue()) {
            Handbrake.processMediaFile(file);
        }

        String overview = "Overview of Completed Operations: \n";
        StringBuilder sb = new StringBuilder();

        sb.append(overview);
        for (Media m : MediaQueue.getMediaQueue()) {
            sb.append("\n" + m.getName() + ": " + m.getCurrentStatus());
            sb.append("\n     Time Elapsed: " + m.getTimeElapsedWhileProcessing().toString() + "\n");
        }

        ConsoleEvent.print(sb.toString());
    }


}

