package general;

import media.MediaQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yjohnson.ConsoleEvent;
import yjohnson.Operations;

import java.io.File;
import java.util.Arrays;

import static general.Main.APP_NAME;
import static general.Main.VERSION;

public class CLI {
	private static final Logger logger = LoggerFactory.getLogger(CLI.class);
	private static MediaQueue queue;

	static void run() {
		CLI.printHeader();
		queue = CLI.createMediaQueueCLI(queue);

		ConsoleEvent.print("Overview of Operations: " + queue.stringOfContents());

		if (ConsoleEvent.askUserForBoolean("Confirm?")) {
			Operations.FileOperation fo = Operations.FileOperation.values()[ConsoleEvent.askUserForOption(
					"Choose an operation",
					Arrays.asList(Operations.FileOperation.toStringArray())
			) - 1];

			Operations op = new Operations();

			ConsoleEvent.print("Starting media queue move operation.");
			File target;
			boolean validDest;
			do {
				target = new File(ConsoleEvent.askUserForString("Input the destination directory"));
				validDest = target.isAbsolute();
				if (!validDest) ConsoleEvent.print("Invalid directory.", ConsoleEvent.logStatus.ERROR);
			} while (!validDest);

			switch (fo) {
				case MOVE_FILE_ATOMICALLY:
					op.ioStandardMoveRunner(queue, target);
					ConsoleEvent.print("Move finalized!");
					break;
				case COPY_FILE_AND_DELETE_SRC:
					op.ioNonAtomicMoveRunner(queue, target);
					break;
				default:
					logger.error("Operation {} not implemented!", fo);
			}

		}

	}

	static void printHeader() {
		logger.info("Starting {}.", APP_NAME);
		/* Intro for user */
		StringBuilder sb = new StringBuilder();
		sb.append('-').append(APP_NAME.toUpperCase()).append(" v").append(VERSION).append('-');
		ConsoleEvent.print(sb.toString());

		sb.setLength(0); // https://stackoverflow.com/questions/5192512/how-can-i-clear-or-empty-a-stringbuilder
		sb.append(APP_NAME).append(" will generate a log file called \"log.out\" in the program's current working directory.");
		ConsoleEvent.print(sb.toString());
	}

	static MediaQueue createMediaQueueCLI(MediaQueue queue) {
		logger.debug("CLI is creating media queue.");
		File src;
		String ext;
		boolean validSrc, validExt;

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
		return queue;
	}

}
