package media;

import java.io.File;

abstract public class Media extends File {
	protected String customName, resolution = "Unknown";

	/**
	 * A superclass that serves as an intermediary between the File and respective media-related subclasses.
	 * Stores the metadata of the files.
	 *
	 * @param path      the path to the Media file
	 * @param mediatype the type of the media (TV, MOVIE, GENERIC)
	 */
	public Media (String path, MediaTypes mediatype) {
		super(path);
	}

	public String getResolution () {
		return resolution;
	}

	public String getCustomName () {
		return customName;
	}

}
