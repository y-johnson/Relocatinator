package general;

import media.Media;
import media.MediaQueue;
import media.Movie;
import media.TV;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.nio.file.StandardCopyOption.ATOMIC_MOVE;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class Operations {
	private static final Logger logger = LoggerFactory.getLogger(Operations.class);

	public static void organizedMove (MediaQueue mQ, File target) {
		logger.info("Starting organized move operation into \"{}\" for given media queue.", target);
		for (MediaQueue.MediaList mL : mQ) {
			logger.trace("Processing Media objects within media list \"{}\".", mL.toString());
			for (Media m : mL) {
				logger.trace("Processing {} object \"{}\".", m.getClass().getName(), m.getCustomName());
				String sb;
				Path newFilePath;
				String o = !target.toString()
				                  .endsWith(File.separator) ? String.valueOf(File.separatorChar) : "";
				switch (m.getType()) {
					case TV:
						TV tv = (TV) m;

						sb = target +
								o +
								tv.getSeriesName() +
								File.separatorChar +
								"Season " +
								tv.getSeasonNumber() +
								File.separatorChar +
								tv.getCustomName() +
								m.getFile().getName().substring(m.getFile().getName().lastIndexOf('.'));

						newFilePath = new File(sb).toPath();
						break;
					case MOVIE:
						Movie movie = (Movie) m;

						sb = target +
								o +
								File.separatorChar +
								movie.getCustomName() +
								m.getFile()
								 .getName()
								 .substring(m.getFile().getName().lastIndexOf('.'));

						newFilePath = new File(sb).toPath();
						break;
					default:
						logger.error("Given type {} has not had \"organized move\" implemented", m.getType());
						throw new UnsupportedOperationException("Unexpected value: " + m.getType());
				}
				moveMedia(m, newFilePath);

			}

		}

	}

	private static void moveMedia (Media from, Path to) {
		logger.trace("Attempting to move {} file from \"{}\" to \"{}\"", from.getType(), from.getFile().getAbsolutePath(), to);
		try {
			Files.createDirectories(to.getParent());
			Path target = Files.move(from.getFile().toPath(), to.toAbsolutePath(), ATOMIC_MOVE, REPLACE_EXISTING);
			if (target.toFile().exists()) {
				from.setFile(target.toFile());
				logger.info("Moved {} to {}.", from.getFile().getName(), to);
			}
		} catch (IOException e) {
			logger.error(
					"An IO exception was thrown while attempting to move a {} file from \"{}\" to \"{}\"",
					from.getType(),
					from.getFile().getAbsolutePath(),
					to
			);
			e.printStackTrace();
		}
	}
}
