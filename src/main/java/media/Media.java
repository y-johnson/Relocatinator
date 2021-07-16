package media;

import java.io.File;
import java.nio.file.Path;

import static media.MetadataOps.unwantedSpaces;

abstract public class Media {
	protected File file;
	protected String customName, resolution = "Unknown";
	protected String ext;
	MediaType type;

	/**
	 * A superclass that serves as an intermediary between the File and respective media-related subclasses. Stores the metadata of the files.
	 *
	 * @param path      the path to the Media file
	 * @param mediatype the type of the media (TV, MOVIE, GENERIC)
	 */
	public Media(String path, MediaType mediatype) {
		this.type = mediatype;
		this.file = new File(path);
		this.ext = this.file.getName().substring(this.file.getName().lastIndexOf("."));
	}

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}

	public MediaType getType() {
		return type;
	}

	public String getMediaResolution() {
		return resolution;
	}

	public String getCustomFilename() {
		return customName;
	}

	/**
	 * Returns a string representation of this media object's customized file path as a child of the given path. No checks are performed to determine
	 * whether a file already exists at that location.
	 *
	 * @param path path to attach this custom file path to.
	 *
	 * @return the absolute path of this media's customized path structure.
	 */
	abstract public Path generateCustomPathStructure(Path path);

	public String getExt() {
		return ext;
	}

	/**
	 * Returns the deduced name of the work that this media object represents. For TV shows, this would be the series' name, whereas for movies, it
	 * would be the movie's title.
	 *
	 * @return the deduced title of this media object.
	 */
	public abstract String getMediaTitle();

	public abstract boolean isValid();

}

