package general;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yjohnson.ConsoleEvent;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Main {

	public static String APP_NAME;
	public static String VERSION;
	public static File PROGRAM_DIR;
	public static Date CURR_DATE;
	private static final Logger logger = LoggerFactory.getLogger(Main.class);
	private static MediaQueue queue;

	static {
		APP_NAME = "Relocatinator";
		VERSION = "0.2";
		PROGRAM_DIR = new File(System.getProperty("user.dir"));
		CURR_DATE = new Date();
	}

	private static void setup () {
		logger.info("Starting {}.", APP_NAME);
		/* Intro for user */
		StringBuilder sb = new StringBuilder();
		sb.append('-').append(APP_NAME.toUpperCase()).append(" v").append(VERSION).append('-');
		ConsoleEvent.print(sb.toString());

		// Clears the string efficiently. https://stackoverflow.com/questions/5192512/how-can-i-clear-or-empty-a-stringbuilder
		sb.setLength(0);
		sb.append(APP_NAME).append(" will generate a log file called \"log.txt\" in the program's current operating directory.");
		ConsoleEvent.print(sb.toString());

	}


	public static void main (String[] args) {
		setup();
		queue = new MediaQueue();

		String overview = "Overview of Operations: ";

		String sb = overview +
				queue.stringOfContents();
		ConsoleEvent.print(sb);

		if (ConsoleEvent.askUserForBoolean("Confirm?")) Operations.organizedMove(queue);

	}


}

