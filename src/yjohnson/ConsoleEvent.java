package yjohnson;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ConsoleEvent {
	private static final Logger consoleLog = LoggerFactory.getLogger(ConsoleEvent.class);
	private static final String INVALID_RESPONSE = "Invalid response. Try again.";
	private static final String SEPARATOR = " / ";
	private static final String ESCAPE_LOOP_KEYWORD = "-DONE-";
	private final static String YES_OR_NO = " [y/n]: ";
	private final static String YES = "Y", NO = "N";
	private static final Scanner sysin;

	/* User input */
	static {
		sysin = new Scanner(System.in);
		System.setProperty("org.slf4j.simpleLogger.logFile", "log.txt");
	}

	/**
	 * Prints out a message on the console.
	 *
	 * @param message message to be printed.
	 */
	public static void print (String message) {
		System.out.print(message + (message.endsWith("\n") ? "" : "\n"));
		consoleLog.info("SYSTEM: {}", message);
	}

	/**
	 * Prints out a message on the console.
	 *
	 * @param message message to be printed.
	 */
	public static void print (String message, logStatus lvl) {
		String s = message + (message.endsWith("\n") ? "" : "\n");
		switch (lvl) {
			case DETAIL:
				consoleLog.debug("{}", message);
				break;
			case NOTICE:
				System.out.print(s);
				consoleLog.info("{}", message);
				break;
			case ERROR:
				System.err.print(s);
				consoleLog.warn("{}", message);
		}
	}

	/**
	 * Initiates a console event that with a given message, waits for the user to input an integer. If an invalid input
	 * is provided, it will return a preset INVALID_RESPONSE and will wait for another input
	 *
	 * @param message the message that prompts the user for an input
	 * @return any integer value above or equal to 0
	 */
	public static int askUserForInt (String message) {
		print(message);

		int userInput;

		do {
			userInput = sysin.nextInt();
			if (userInput >= 0) {
				// When user input is valid, log it
				logUserInput(String.valueOf(userInput));
				return userInput;
			}
			print(INVALID_RESPONSE);
		} while (true);
	}

	/**
	 * Initiates a console event that with a given message, waits for the user to input an option from a given array
	 * list. If an invalid input is provided, it will return a preset INVALID_RESPONSE and will wait for another input
	 *
	 * @param message the message that prompts the user for an input
	 * @return any integer value that corresponds to an option
	 */
	public static int askUserForOption (String message, List<String> options) {
		if (message.endsWith(":")) {
			message += " ";
		} else if (!message.endsWith(": ")) {
			message = message.trim() + ": ";
		}
		StringBuilder messageBuilder = new StringBuilder(message);

		messageBuilder.append("\n");
		for (int i = 1; i <= options.size(); ++i) {
			messageBuilder.append(String.format("%d. %s", i, options.get(i - 1)));
			if (i < options.size()) {
				messageBuilder.append(SEPARATOR);
			}
		}
		messageBuilder.append("\n");

		message = messageBuilder.toString();
		print(message);

		/* Get next user input as an integer */
		int userInput;
		do {
			userInput = sysin.nextInt();
			for (int i = 1; i <= options.size(); ++i) {
				if (userInput == i) {
					/* If a match is found, return that number */
					// When user input is valid, log it
					logUserInput(String.valueOf(i));
					return i;
				}
			}
			print(INVALID_RESPONSE);
		} while (userInput <= 0 || userInput > options.size());

		/* Not valid option, never reached */
		return -1;
	}

	/**
	 * Initiates a console event that with a given message, waits for the user to input a string.
	 *
	 * @param message the message that prompts the user for an input
	 * @return the string that the user input.
	 */
	public static String askUserForString (String message) {
		if (message.endsWith(":")) {
			message += " ";
		} else if (!message.endsWith(": ")) {
			message = message.trim() + ": ";
		}

		print(message);
		String newString = sysin.nextLine();

		logUserInput(newString);
		return newString;
	}

	public static ArrayList<String> askUserForArguments (String message) {
		print(message);
		print("Type \"" + ESCAPE_LOOP_KEYWORD + "\" to proceed.");
		ArrayList<String> args = new ArrayList<>();
		while (sysin.hasNext()) {
			String newArg = sysin.next();

			if (newArg.equals(ESCAPE_LOOP_KEYWORD)) {
				break;
			}
			args.add(newArg);
		}

		logUserInput(args.toString());

		return args;
	}

	/**
	 * Asks the user for a yes or no answer for the given message.
	 * 'y' = True
	 * 'n' = False
	 *
	 * @param message a message to prompt the user for the boolean
	 * @return true if user wrote 'y', false otherwise
	 */
	public static boolean askUserForBoolean (String message) {
		message = message.strip();
		print(message + YES_OR_NO);

		String userInput = sysin.next().trim();

		/* While input is not valid... */
		while (!userInput.equalsIgnoreCase(YES) && !userInput.equalsIgnoreCase(NO)) {
			print(INVALID_RESPONSE);
			userInput = sysin.next();
		}

		boolean userAnswer = userInput.equalsIgnoreCase("y");

		// When user input is valid, log it
		logUserInput(String.valueOf(userAnswer));

		return userAnswer;
	}

	/**
	 * Prints a given ArrayList
	 *
	 * @param list String ArrayList
	 */
	public static void printList (ArrayList<String> list) {
		StringBuilder sb = new StringBuilder();
		for (String e : list) {
			sb.append(e).append("\n");
		}

		print(sb.toString());
	}

	private static void logUserInput (String userInput) {
		consoleLog.info("USER: " + userInput + "\n");
	}

	public static void closeProgram (String message, int exitCode) {
		print(message, logStatus.ERROR);
		print("Program will now close.", logStatus.ERROR);
		System.exit(exitCode);
	}

	public enum logStatus {
		ERROR, NOTICE, DETAIL
	}
}
