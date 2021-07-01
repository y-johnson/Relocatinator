package general;

import media.MediaQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yjohnson.ConsoleEvent;

import java.io.File;
import java.util.Date;

public class Main {

	private static final Logger logger = LoggerFactory.getLogger(Main.class);
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
		logger.info("Starting {}.", APP_NAME);
		/* Intro for user */
		StringBuilder sb = new StringBuilder();
		sb.append('-').append(APP_NAME.toUpperCase()).append(" v").append(VERSION).append('-');
		ConsoleEvent.print(sb.toString());

		// Clears the string efficiently. https://stackoverflow.com/questions/5192512/how-can-i-clear-or-empty-a-stringbuilder
		sb.setLength(0);
		sb.append(APP_NAME).append(" will generate a log file called \"log.out\" in the program's current working directory.");
		ConsoleEvent.print(sb.toString());

	}

	private static void userMediaQueueCLI () {
		logger.debug("User is creating media queue.");
		File src;
		String ext;
		boolean validSrc, validExt, validDest;

		do {
			do {
				src = new File(ConsoleEvent.askUserForString("Input the source directory").trim());
				validSrc = src.isDirectory();
				if (!validSrc) ConsoleEvent.print("Invalid directory.", ConsoleEvent.logStatus.ERROR);
			} while (!validSrc);

			do {
				ext = ConsoleEvent.askUserForString("Input the file extension (e.g. '.mkv', '.mp4',...). It must start with a '.'");
				validExt = ext.startsWith(".");
				if (!validExt) ConsoleEvent.print("Invalid extension.", ConsoleEvent.logStatus.ERROR);
			} while (!validExt);

			queue = new MediaQueue(src.toPath(), ext, ConsoleEvent.askUserForMediaType());

		} while (ConsoleEvent.askUserForBoolean("Add more files to the queue?"));


	}

	public static void main (String[] args) {
		setup();
		userMediaQueueCLI();
		String overview = "Overview of Operations: ";

		String sb = overview + queue.stringOfContents();
		ConsoleEvent.print(sb);



		if (ConsoleEvent.askUserForBoolean("Confirm?")) {
			ConsoleEvent.print("Starting media queue move operation.");
			File target;
			boolean validDest;
			do {
				target = new File(ConsoleEvent.askUserForString("Input the destination directory"));
				validDest = target.isAbsolute();
				if (!validDest) ConsoleEvent.print("Invalid directory.", ConsoleEvent.logStatus.ERROR);
			} while (!validDest);
			Operations.organizedMove(queue, target);
		}

	}


}

