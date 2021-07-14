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

	public static void main(String[] args) {
		CLI.run();

	}


}

