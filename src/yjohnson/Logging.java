package yjohnson;
import java.io.*;
import java.util.logging.*;

public class Logging {

    /* The name of the log */
    private static final String LOG_NAME = "log.txt";
    private static Logger logger = null;

    /* This is the file handler that all logs must use to append to the main log*/
    public static FileHandler fileHandler;

    static {
        try {
            fileHandler = new FileHandler(LOG_NAME);
            fileHandler.setFormatter(new SimpleFormatter());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
     * [%1$tF %1$tT] [%4$-7s] %5$s %n
     */
    static {
        if (new File(LOG_NAME).exists()) new File(LOG_NAME).delete();

        InputStream stream = Logging.class.getClassLoader().getResourceAsStream("logging.properties");
        try {
            LogManager.getLogManager().readConfiguration(stream);
            logger = Logger.getLogger(Logging.class.getName());

            logger.addHandler(fileHandler);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Logger getLogger() {
        return logger;
    }


}
