package media;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yjohnson.Checksum;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;

public class Operations {
	private static final Logger logger = LoggerFactory.getLogger(Operations.class);

	public static void organizedMove(MediaQueue mQ, File destinationDir) {
		logger.info("Starting organized move operation into \"{}\" for given media queue.", destinationDir);
		for (MediaQueue.MediaList mediaList : mQ) {
			logger.trace("Processing Media objects within media list \"{}\".", mediaList.toString());
			for (Media mediaObj : mediaList) {
				MediaIOWrapper wrapper;
				Path newFilePath;
				logger.trace("Processing {} object \"{}\".", mediaObj.getClass().getName(), mediaObj.getCustomFilename());

				newFilePath = mediaObj.generateCustomPathStructure(destinationDir.toPath());
				wrapper = new MediaIOWrapper(mediaObj, mediaObj.getFile().toPath(), newFilePath);
				try {
					if (copyMedia(wrapper)) {
						wrapper.media.setFile(newFilePath.toFile());
						logger.debug(
								"Successfully copied \"{}\" over to destination; deleting original file (at = \"{}\").",
								wrapper.media.getCustomFilename(),
								wrapper.from
						);

						removeSourceFilePostMove(wrapper);
					}

				} catch (FileAlreadyExistsException e) {
					logger.error(
							"Media copy operation failed; there is already a file in the target destination (target = {}).",
							wrapper.to.toAbsolutePath()
					);
					logger.error(e.toString());
					e.printStackTrace();
				} catch (SecurityException e) {
					logger.error(
							"Media copy operation failed; the operation was interrupted by the security manager (target = {}).",
							wrapper.to.toAbsolutePath()
					);
					logger.error(e.toString());
					e.printStackTrace();
				} catch (IOException e) {
					logger.error(
							"An IO exception was thrown while attempting to copy a {} file from \"{}\" to \"{}\"",
							wrapper.media.getType(),
							wrapper.media.getFile().getAbsolutePath(),
							wrapper.to
					);
					logger.error(e.toString());
					e.printStackTrace();
				}
			}

		}

	}


	/**
	 * Initiates a copy operation using the Java NIO package's Files class. Using the MediaIOWrapper class, it will validate a checksum for the
	 * operation; if the destination file was successfully copied and has an equivalent checksum, the method returns true.
	 * <p></p>
	 * When either of the above are false, the method returns false. The method will not attempt to delete the erroneously copied file in such cases.
	 *
	 * @param wrapper the MediaIOWrapper that encapsulates the Media to be moved.
	 *
	 * @return true if copy operation (and checksum validation) succeeded, false otherwise.
	 */
	private static boolean copyMedia(MediaIOWrapper wrapper) throws IOException {
		logger.info(
				"Copying {} file from \"{}\" to \"{}\".",
				wrapper.media.getType(),
				wrapper.media.getFile().getAbsolutePath(),
				wrapper.to
		);
		if (wrapper.to.toFile().exists()) {
			logger.warn("File already exists at \"{}\"; move operation may fail.", wrapper.to
			);
		}

		Files.createDirectories(wrapper.to.getParent());
		logger.trace("Created/verified directories \"{}\".", wrapper.to.getParent());

		logger.info("Initiating file copy (from = {}, to = {}).", wrapper.media.getFile().getName(), wrapper.to);
		Path target = Files.copy(wrapper.media.getFile().toPath(), wrapper.to.toAbsolutePath());
		if (target.toFile().exists() && wrapper.validateChecksum()) {
			wrapper.media.setFile(target.toFile());
			logger.info("Copied {} to {}.", wrapper.media.getFile().getName(), wrapper.to);
			return true;
		}


		return false;
	}

	private static void removeSourceFilePostMove(MediaIOWrapper wrapper) {
		try {
			Files.delete(wrapper.from);
			if (Files.notExists(wrapper.from)) {
				logger.debug(
						"Successfully deleted \"{}\" from original source (at = \"{}\").",
						wrapper.media.getCustomFilename(),
						wrapper.from
				);
			} else {
				logger.warn(
						"Failed to delete \"{}\" from original source (at = \"{}\").",
						wrapper.media.getCustomFilename(),
						wrapper.from
				);
			}
		} catch (NoSuchFileException e) {
			logger.warn(
					"File \"{}\" does not exist, but it was not deleted by this program. Subsequent operations may fail if interfered with.",
					wrapper.from
			);
			logger.warn(e.toString());
			e.printStackTrace();
		} catch (IOException e) {
			logger.error(
					"An IO exception was thrown while attempting to delete the original file from an organized move operation (orig. = \"{}\").",
					wrapper.from
			);
			logger.error(e.toString());
			e.printStackTrace();
		}
	}

	private static class MediaIOWrapper {
		private final Media media;
		private final Path from, to;
		String checksum;

		public MediaIOWrapper(Media media, Path from, Path to) {
			this.media = media;
			this.from = from;
			this.to = to;
			try {
				checksum = Checksum.getChecksum(from.toString());
			} catch (Exception e) {
				logger.error("Error when calculating checksum for \"{}\".", media.getMediaTitle());
				e.printStackTrace();
			}
		}

		private boolean validateChecksum() {
			logger.debug("Validating checksum for media {}.", media.getCustomFilename());
			if (!this.from.toFile().exists() || !this.to.toFile().isFile()) return false;

			try {
				if (this.checksum.equals(Checksum.getChecksum(this.to.toString()))) {
					logger.info("MD5 checksum (MD5 = {}) matches for media {}.", this.checksum, media.getCustomFilename());
					return true;
				} else {
					logger.warn(
							"MD5 checksum failed for for media {}. (expected = {}, actual = {}",
							media.getCustomFilename(),
							this.checksum,
							Checksum.getChecksum(this.to.toString())
					);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return false;
		}
	}
}
