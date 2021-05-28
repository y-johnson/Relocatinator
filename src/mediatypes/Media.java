package mediatypes;

import java.io.File;
import java.io.IOException;
import java.time.Duration;

abstract public class Media extends File {

    // VARIABLES
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public static final String NOT_APPLICABLE = "Not Applicable";
    protected static final String[] unwantedSpaces = {".", "_"};
    protected static final String[] unwantedBrackets = {"[", "]", "(", ")"};
    private final type mediaType;
    /**
     * Keeps track of the time it took HandBrake to process the media file.
     */
    private Duration timeElapsedWhileProcessing;
    private status currentStatus = status.NOT_PROCESSED;
    /**
     * 0 = EXTERNAL SUBTITLE LOCATION ("**NO EXT. SUBS**" if none found)
     * 1 = NAME OF SERIES OR MOVIE
     * 2 = SEASON NUMBER ("Not Applicable" if not a TV object)
     * 3 = EPISODE NUMBER ("Not Applicable" if not a TV object)
     * 4 = SOURCE LOCATION
     * 5 = DESTINATION LOCATION
     * 6 = CLEAN NAME (name without extra data)
     * 7 = RESOLUTION
     */
    private String[] metadata;
    /**
     * A superclass that serves as an intermediary between the File and respective media-related subclasses.
     * Stores the metadata of the files.
     *
     * @param pathname  the path to the Media file
     * @param mediatype the type of the media (TV, MOVIE, GENERIC)
     */
    public Media(String pathname, type mediatype) {
        super(pathname);
        mediaType = mediatype;
    }

    public Duration getTimeElapsedWhileProcessing() {
        return timeElapsedWhileProcessing;
    }

    public void setTimeElapsedWhileProcessing(Duration timeElapsedWhileProcessing) {
        this.timeElapsedWhileProcessing = timeElapsedWhileProcessing;
    }

    public Media.status getCurrentStatus() {
        return currentStatus;
    }

    // CONSTRUCTORS
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public void setCurrentStatus(Media.status newStatus) {
        this.currentStatus = newStatus;
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    // METHODS
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Allows for the storage of subtitle file information.
     *
     * @param subLocation the absolute path to the subtitle file for this media file.
     */
    public void setSubs(String subLocation) {
        metadata[0] = subLocation;
    }

    /**
     * Returns metadata, the full array of information gathered from the file.
     *
     * @return
     */
    public String[] getMetadata() {
        return metadata;
    }

    /**
     * Allows for the storage of metadata.
     *
     * @param metadata the appropriately formatted metadata String array for the object.
     */
    public void setMetadata(String[] metadata) {
        this.metadata = metadata;
    }

    /**
     * Returns metadata[6], the name (after parsing) of the file.
     *
     * @return the media's proper name
     */
    public String getName() {
        return metadata[6];
    }

    /**
     * Returns mediaType, the enum that states which type of media this is.
     *
     * @return the media's proper name
     */
    public type getMediaType() {
        return mediaType;
    }

    /**
     * Abstract method of the Media class. All subclasses must implement a way to get information out of the given file's
     * name, such as its resolution, the season, the release year, etc. This must stored in a String array of equal length
     * and formatting to any array given by Metadata.mdDesc.
     *
     * @param filename the absolute path to the file
     * @return a string array that contains metadata to be used by other processes.
     * @throws IOException
     */
    public abstract String[] extractTitleInfo(String filename) throws IOException;

    /**
     * The three media types to assign to all subclasses of Media.
     */
    public enum type {TV, MOVIE, GENERIC}

    /**
     * The various states that the media can be in throughout the program's lifespan.
     */
    public enum status {PROCESSED, NOT_PROCESSED, PROCESSING, ERROR, PERMISSIONS_ERROR}

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

}
