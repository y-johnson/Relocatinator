package general;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class Main {

	private static final Logger logger = LoggerFactory.getLogger(Main.class);
	public static String APP_NAME;
	public static String VERSION;

	static {
		APP_NAME = "Relocatinator";
		VERSION = "0.2";
	}

	public static void main(String[] args) {
		logger.info("{} V{}: Starting CLI application.", APP_NAME, VERSION);
		CLI.run();

	}


}

