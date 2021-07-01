package media;

import java.io.File;

abstract public class Media {
	protected File file;
	protected String customName, resolution = "Unknown";
	MediaType type;
	/**
	 * A superclass that serves as an intermediary between the File and respective media-related subclasses.
	 * Stores the metadata of the files.
	 *
	 * @param path      the path to the Media file
	 * @param mediatype the type of the media (TV, MOVIE, GENERIC)
	 */
	public Media (String path, MediaType mediatype) {
		this.type = mediatype;
		this.file = new File(path);
	}

	public File getFile () {
		return file;
	}

	public void setFile (File file) {
		this.file = file;
	}

	public MediaType getType () {
		return type;
	}

	public String getResolution () {
		return resolution;
	}

	public String getCustomName () {
		return customName;
	}

	public abstract boolean isValid();
}

