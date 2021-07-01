package yjohnson;

import java.io.*;

public class TextFinder {
    /**
     * Looks for a string inside a file with a FileReader pointing at fileToSearch BufferedReader.
     * @param s string to search for.
     * @param fileToSearch file to search in.
     * @param caseSensitive if the search is case sensitive or not.
     * @return true if string was found, false otherwise.
     */
    public static boolean isStringInFile (String s, File fileToSearch, boolean caseSensitive) {

        try {
            FileReader fileIn = new FileReader(fileToSearch);
            BufferedReader reader = new BufferedReader(fileIn);
            String line;

            if (caseSensitive) {
                while((line = reader.readLine()) != null) {
                    if((line.contains(s))) {
                        return true;
                    }
                }
            } else {
                while((line = reader.readLine()) != null) {
                    if((line.toLowerCase().contains(s.toLowerCase()))) {
                        return true;
                    }
                }
            }

        } catch (FileNotFoundException e) {
            ConsoleEvent.print("Could not locate " + fileToSearch + ". Returning as false.", ConsoleEvent.logStatus.ERROR);
            return false;
        } catch (IOException e) {
            ConsoleEvent.print("I/O error while attempting to read a line with BufferedReader. Returning as false.", ConsoleEvent.logStatus.ERROR);
        }
        return false;
    }
}
