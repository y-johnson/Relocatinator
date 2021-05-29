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

    private static void setup() {
        /* Intro for user */
        StringBuilder sb = new StringBuilder();
        sb.append('-').append(APP_NAME.toUpperCase()).append(" v").append(VERSION).append('-').append("\n");
        ConsoleEvent.print(sb.toString());

        ConsoleEvent.setShowUserAllEvents(ConsoleEvent.askUserForBoolean("Show debug text?"));

        // Clears the string efficiently. https://stackoverflow.com/questions/5192512/how-can-i-clear-or-empty-a-stringbuilder
        sb.setLength(0);
        sb.append(APP_NAME).append(" will generate a log file called \"log.txt\" in the program's current operating directory.\n");
        ConsoleEvent.print(sb.toString());

        /* TODO
        The following code-block can (and should) be extracted to facilitate abstraction. Because it is dedicated to
        HandBrake, making it a method of the HandBrake class would be best.
         */
        {
            boolean correct = false;
            do {
                File h;
                do {
                    h = new File(ConsoleEvent.askUserForString("Location for HandBrake (CLI version): "));
                } while (!h.exists() || !Handbrake.isPresent(h));
                handbrakeLocation = h;

                sb = new StringBuilder();
                sb.append("\nHandBrake location: ").append(handbrakeLocation.getAbsolutePath()).append("\n");
                ConsoleEvent.print(sb.toString());

                correct = ConsoleEvent.askUserForBoolean("Confirm?");
            } while (!correct);
        }

        SimpleDateFormat formatter = new SimpleDateFormat(" [dd-MM-yyyy][HH-mm-ss]");
        MediaHistory.setup("History" + formatter.format(CURR_DATE) + ".txt");

        MediaQueue.createMediaQueue();

        // TODO Same as above
        {
            Handbrake.setAdditionalArgs();
        }
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

