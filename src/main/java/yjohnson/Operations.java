package yjohnson;

import media.Media;
import media.MediaQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;

import static java.nio.file.StandardCopyOption.ATOMIC_MOVE;

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
					Files.move(wrapper.from.toAbsolutePath(), wrapper.to, ATOMIC_MOVE);
					logger.info("Successfully moved \"{}\" to \"{}\".", wrapper.media.getCustomFilename(), wrapper.to);

				} catch (NoSuchFileException e) {
					logger.error("Java NIO reports a {}.", e.toString());
					logger.error(e.toString());
					e.printStackTrace();
				} catch (AtomicMoveNotSupportedException e) {
					logger.warn(
							"Could not move file atomically with Java NIO implementation (not supported). It is likely that the source and target destinations are in different FileStores.");
					logger.warn("Attempting \"safe\" non-atomic move w/ checksum validation.");
					safeNonAtomicMove(wrapper, newFilePath);
				} catch (IOException e) {
					logger.error(
							"An IO exception was thrown while attempting to move a {} file from \"{}\" to \"{}\".",
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

	private static void safeNonAtomicMove(MediaIOWrapper wrapper, Path newFilePath) {
		logger.debug(
				"Attempting safe move operation on {} object {} (from = \"{}\", to = \"{}\").",
				wrapper.media.getClass().getSimpleName(),
				wrapper.media.getCustomFilename(),
				wrapper.from,
				wrapper.to
		);
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
			wrapper.from.toFile();

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
					"An IO exception was thrown while attempting to copy a {} file from \"{}\" to \"{}\".",
					wrapper.media.getType(),
					wrapper.media.getFile().getAbsolutePath(),
					wrapper.to
			);
			logger.error(e.toString());
			e.printStackTrace();
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
			logger.warn("File already exists at \"{}\"; copy operation may fail.", wrapper.to
			);
		}

		if (wrapper.to.getParent().toFile().exists()) {
			Files.createDirectories(wrapper.to.getParent());
			logger.trace("Created/verified directories \"{}\".", wrapper.to.getParent());

		}

		logger.debug("Initiating file copy (media = {}, to = {}).", wrapper.media.getCustomFilename(), wrapper.to);
		Path target = Files.copy(wrapper.media.getFile().toPath(), wrapper.to.toAbsolutePath());
		if (target.toFile().exists() && wrapper.validateChecksum()) {
			wrapper.media.setFile(target.toFile());
			logger.info("Copied \"{}\" to {}.", wrapper.media.getCustomFilename(), wrapper.to);
			return true;
		}
		logger.error("Failed to copy file (media = {}, to = {}).", wrapper.media.getCustomFilename(), wrapper.to);


		return false;
	}

	private static void removeSourceFilePostMove(MediaIOWrapper wrapper) {
		logger.trace("Deleting file at original location (source = \"{}\").", wrapper.from);
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
				logger.trace(
						"Created new {} instance (media = {}, from = \"{}\", to = \"{}\").",
						this.getClass().getSimpleName(),
						media.getCustomFilename(),
						this.from,
						this.to
				);
				checksum = Checksum.getChecksum(from.toString());
			} catch (Exception e) {
				logger.error("Error when calculating checksum for \"{}\".", media.getMediaTitle());
				e.printStackTrace();
			}
		}

		private boolean validateChecksum() {
			logger.debug("Validating checksum for media {}.", media.getCustomFilename());
			if (!this.from.toFile().isFile() || !this.to.toFile().isFile()) {
				logger.warn("Either source or target file does not exist. (source = {}, target = {})", this.from, this.to);
				return false;
			}

			try {
				if (this.checksum.equals(Checksum.getChecksum(this.to.toString()))) {
					logger.debug("MD5 checksum (MD5 = {}) matches for media {}.", this.checksum, media.getCustomFilename());
					return true;
				} else {
					logger.warn(
							"MD5 checksum failed for media {}. (expected = {}, actual = {})",
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
