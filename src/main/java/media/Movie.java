package media;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Movie extends Media {
	private static final Logger logger = LoggerFactory.getLogger(Movie.class);

	private static final int UNKNOWN_RELEASE_YEAR = -1;
	private static final char[] OS_YEAR = String.valueOf(Calendar.getInstance().get(Calendar.YEAR)).trim().toCharArray();
	private static final Pattern regexReleaseYear = Pattern.compile(
			"\\b(?<year>1[8-9][0-9][0-9]|2[0-" + OS_YEAR[1] + "][0-" + OS_YEAR[2] + "][0-9])\\b");


	private int releaseYear = 0;
	private String movieName;

	/**
	 * A superclass that serves as an intermediary between the File and respective media-related subclasses. Stores the metadata of the files.
	 *
	 * @param path the path to the Media file
	 */
	public Movie(String path) {
		super(path, MediaType.MOVIE);
		this.extractTitleInfo();
		logger.info("Created \"{}\" ({}) from source file \"{}\"", this.getCustomFilename(), this.getClass().getName(), this.getFile().getName());
	}

	/**
	 * Extracts relevant information out of the name of the file. This includes the name of the show, the season the file corresponds to, and the
	 * episode number.
	 */
	public void extractTitleInfo() {
		logger.info("Parsing Movie information for \"{}\".", this.getFile().getName());

		this.resolution = MetadataOps.getResolution(this.file.getName());
		parseReleaseYear();

		if (this.releaseYear == UNKNOWN_RELEASE_YEAR) {
			String name = MetadataOps
					.removeUnwantedSpaces(
							MetadataOps.removeBrackets(
									this.file.getName()
									         .substring(0, this.file.getName().lastIndexOf('.'))
							)
					).trim();
			this.movieName = this.customName = name;
		} else {
			String name = MetadataOps
					.removeUnwantedSpaces(
							MetadataOps.removeBrackets(
									this.file.getName()
									         .substring(0, this.file.getName().lastIndexOf(Integer.toString(this.releaseYear)))))
					.trim();
			this.movieName = name;
			this.customName = name + " (" + releaseYear + ")";
		}

		logger.debug(
				"Parsed movie name as \"{}\" with release year of {} (src = \"{}\").",
				this.movieName,
				this.releaseYear,
				this.getFile().getName()
		);
		logger.trace(
				"Proposed custom name \"{}\" --> \"{}\"",
				this.getFile().getName().substring(0, this.getFile().getName().lastIndexOf('.')),
				this.customName
		);
	}

	private void parseReleaseYear() {
		String fileName = this.file.getName();
		logger.debug("Retrieving movie release year from file name (filename = \"{}\").", fileName);
		logger.trace("Valid movie release year range: 1800 - {}.", Calendar.getInstance().get(Calendar.YEAR) + 1);
		Matcher matcher = regexReleaseYear.matcher(fileName);
		if (matcher.find() && matcher.group("year") != null) {
			logger.debug("Release year pattern matched against file \"{}\" with a result of {}.", fileName, matcher.group("year"));
			this.releaseYear = Integer.parseInt(matcher.group("year"));
		} else {
			String parentDir = this.file.getParentFile().getName();
			logger.warn("File name did not match a release year. Attempting to match parent directory (dir = \"{}\").", parentDir);
			matcher = regexReleaseYear.matcher(parentDir);
			if (matcher.find() && matcher.group("year") != null) {
				logger.debug("Release year pattern matched against parent directory \"{}\" with a result of {}.", parentDir, matcher.group("year"));
				this.releaseYear = Integer.parseInt(matcher.group("year"));
			} else {
				logger.warn(
						"Parent directory name did not match a release year. Setting \"Unknown\" value '{}' to release year (dir = \"{}\").",
						UNKNOWN_RELEASE_YEAR,
						parentDir
				);
				this.releaseYear = UNKNOWN_RELEASE_YEAR;
			}
		}
	}

	public int getReleaseYear() {
		return releaseYear;
	}

	public String getMovieName() {
		return movieName;
	}

	/**
	 * Returns a string representation of this media object's customized file path as a child of the given path. No checks are performed to determine
	 * whether a file already exists at that location.
	 *
	 * @param path path to attach this custom file path to.
	 *
	 * @return the absolute path of this media's customized path structure.
	 */
	@Override
	public Path generateCustomPathStructure(Path path) {
		Path generated = new File(
				path +
						(path.toString().endsWith(File.separator) ? "" : String.valueOf(File.separatorChar)) +
						this.getMediaTitle() +
						File.separatorChar +
						this.getCustomFilename() +
						this.getExt()
		).toPath();

		logger.trace(
				"Generated path for TV object \"{}\" (given parent path = \"{}\", generated = \"{}\".",
				this.getCustomFilename(),
				path,
				generated
		);

		return generated.toAbsolutePath();
	}

	@Override
	public String getMediaTitle() {
		return this.movieName;
	}

	@Override
	public boolean isValid() {
		/* If the file exists and both the movie name and custom name exist, then it is a valid Movie object */
		return this.getFile().isFile() && !this.movieName.isEmpty() && !this.customName.isEmpty();
	}
}
