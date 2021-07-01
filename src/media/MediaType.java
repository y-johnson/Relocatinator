package media;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

/**
 * The three media types to assign to all subclasses of Media.
 */
public enum MediaType {
	TV {
		@Override
		public TV instantiate (Path path) {
			if (path.toFile().isFile()) return new TV(path.toAbsolutePath().toString());
			else {
				logger.error("Cannot instantiate new {} object; path (path = \"{}\") does not lead to a file.", this, path);
				throw new IllegalArgumentException("Given path does not lead to a file.");
			}
		}
	},
	MOVIE {
		@Override
		public Movie instantiate (Path path) {
			if (path.toFile().isFile()) return new Movie(path.toAbsolutePath().toString());
			else {
				logger.error("Cannot instantiate new {} object; path (path = \"{}\") does not lead to a file.", this, path);
				throw new IllegalArgumentException("Given path does not lead to a file.");
			}
		}
	};

	private static final Logger logger = LoggerFactory.getLogger(MediaType.class);

	/**
	 * Instantiates a new media object with the given path of a file.
	 *
	 * @param path a valid path to a file object.
	 * @return the newly formed Media subclass object.
	 */
	public abstract Media instantiate (Path path);

}
