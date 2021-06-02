package mediatypes;

import yjohnson.ConsoleEvent;

import java.util.ArrayList;
import java.util.Arrays;

public class GenericVideo extends Media {
    /**
     * A superclass that serves as an intermediary between the File and respective media-related subclasses.
     * Stores the metadata of the files.
     *
     * @param path      the path to the Media file
     * @param mediatype the type of the media (TV, MOVIE, GENERIC)
     */
    public GenericVideo (String path, MediaTypes mediatype) {
        super(path, mediatype);
    }
//
//    /**
//     * Creates a Generic Video object, a subclass of Media, that functions the same as a File with added functionality, such as
//     * metadata storage and references to other files.
//     * Generic Video requires user input for its metadata information. Furthermore, due to the program's current order,
//     * it is currently impossible to do all the metadata first and then do the HandBrake procedures.
//     *
//     * @param pathname the path to the Media file
//     */
//    public GenericVideo(String pathname) {
//
//        super(pathname, MediaTypes.GENERIC);
//        super.setMetadata(extractTitleInfo(pathname));
//    }
//
//    /**
//     * Prompts the user to input individual metadata.
//     * @param filename the absolute path to the file
//     * @return the metadata string array;
//     */
//    public String[] extractTitleInfo(String filename) {
//
//        // Metadata Array
//        String [] md = new String[MetadataOps.mdDesc(MediaTypes.GENERIC).length];
//        String [] mdDescription = MetadataOps.mdDesc(MediaTypes.GENERIC);
//        String [] mdWithDescription = new String[MetadataOps.mdDesc(MediaTypes.GENERIC).length];
//
//        for (int i = 0; i < MetadataOps.mdDesc(MediaTypes.GENERIC).length; ++i) {
//            md[i] = ConsoleEvent.askUserForString(" " + mdDescription[i] + ": ").trim();
//            mdWithDescription[i] = (" " + mdDescription[i] + ": " + md[i]).trim();
//        }
//
//        ConsoleEvent.printList( (ArrayList<String>) Arrays.asList(mdWithDescription) );
//
//        if (!ConsoleEvent.askUserForBoolean("Is the above metadata correct?")) {
//            md = extractTitleInfo(filename);
//        }
//
//        return md;
//
//    }
}
