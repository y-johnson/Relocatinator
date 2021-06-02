package general;

import yjohnson.ConsoleEvent;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Main {

	public static String APP_NAME;
	public static String VERSION;
	public static File PROGRAM_DIR;
	public static Date CURR_DATE;
	private static MediaQueue queue;

	static {
		APP_NAME = "Relocatinator";
		VERSION = "0.2";
		PROGRAM_DIR = new File(System.getProperty("user.dir"));
		CURR_DATE = new Date();
	}

	private static void setup () {
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
//        {
//            boolean correct = false;
//            do {
//                File h;
//                do {
//                    h = new File(ConsoleEvent.askUserForString("Location for HandBrake (CLI version): "));
//                } while (!h.exists() || !Handbrake.isPresent(h));
//                handbrakeLocation = h;
//
//                sb = new StringBuilder();
//                sb.append("\nHandBrake location: ").append(handbrakeLocation.getAbsolutePath()).append("\n");
//                ConsoleEvent.print(sb.toString());
//
//                correct = ConsoleEvent.askUserForBoolean("Confirm?");
//            } while (!correct);
//        }

		SimpleDateFormat formatter = new SimpleDateFormat(" [dd-MM-yyyy][HH-mm-ss]");

		queue = new MediaQueue();

	}


	public static void main (String[] args) {

		setup();

		String overview = "Overview of Completed Operations: \n";
		StringBuilder sb = new StringBuilder();
		sb.append(overview);
		sb.append(queue.stringOfContents());

		ConsoleEvent.print(sb.toString());
	}


}

