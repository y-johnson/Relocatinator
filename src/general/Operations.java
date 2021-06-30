package general;

import media.Media;
import media.Movie;
import media.TV;
import yjohnson.ConsoleEvent;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.nio.file.StandardCopyOption.ATOMIC_MOVE;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class Operations {

	public static void organizedMove (MediaQueue mQ) {
		for (MediaQueue.MediaList mL : mQ) {
			for (Media m : mL) {
				String sb;
				Path p;
				switch (m.getType()) {
					case TV:
						TV tv = (TV) m;

						sb = mQ.getDestinationDir().toString() +
								(!mQ.getDestinationDir().toString().endsWith(File.separator) ? File.separatorChar : "") +
								tv.getSeriesName() +
								File.separatorChar +
								"Season " + tv.getSeasonNumber() +
								File.separatorChar +
								tv.getCustomName() +
								m.getFile().getName().substring(m.getFile().getName().lastIndexOf('.'));

						p = new File(sb).toPath();
						break;
					case MOVIE:
						Movie movie = (Movie) m;

						sb = mQ.getDestinationDir().toString() +
								(!mQ.getDestinationDir().toString().endsWith(File.separator) ? File.separatorChar : "") +
								File.separatorChar +
								movie.getCustomName() +
								m.getFile().getName().substring(m.getFile().getName().lastIndexOf('.'));

						p = new File(sb).toPath();
						break;
					default:
						throw new IllegalStateException("Unexpected value: " + m.getType());
				}
				moveMedia(m, p);

			}

		}

	}

	private static void moveMedia (Media from, Path to) {
		try {
			Files.createDirectories(to.getParent());
			Path target = Files.move(from.getFile().toPath(), to.toAbsolutePath(), ATOMIC_MOVE, REPLACE_EXISTING);
			if (target.toFile().exists()) {
				from.setFile(target.toFile());
				ConsoleEvent.print("Moved " + from.getFile().getName() + " to " + to + "\n");

			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
