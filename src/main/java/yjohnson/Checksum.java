package yjohnson;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Credit: Réal Gagnon for the checksum generation and conversion code
 * @see <a href=https://www.rgagnon.com/javadetails/java-0416.html>Real's How-to</a>
 */
public class Checksum {
	private static final Logger logger = LoggerFactory.getLogger(Checksum.class);
	private static final String algorithm = "MD5";

	public static String getChecksum(String filename) throws IOException {
		logger.debug("Generating checksum for \"{}\".", filename);
		byte[] b = createChecksum(filename);

		StringBuilder result = new StringBuilder();

		for (byte value : b) {
			result.append(Integer.toString((value & 0xff) + 0x100, 16).substring(1));
		}
		logger.debug("Generated checksum for file \"{}\" (checksum = {}).", filename, result);
		return result.toString();
	}

	public static byte[] createChecksum(String filename) throws IOException {
		logger.trace("Creating checksum (filename = \"{}\").", filename);

		InputStream fis = new FileInputStream(filename);
		byte[] buffer = new byte[1024];
		MessageDigest complete = null;

		try {
			complete = MessageDigest.getInstance(algorithm);
		} catch (NoSuchAlgorithmException e) {
			logger.error(
					"Failed to get instance for MessageDigest due to an {} using algorithm name \"{}\"",
					e.getClass().getSimpleName(),
					algorithm
			);
			logger.error(e.toString());
			e.printStackTrace();
		}
		int numRead;

		do {
			numRead = fis.read(buffer);
			if (numRead > 0) {
				assert complete != null;
				complete.update(buffer, 0, numRead);
			}
		} while (numRead != -1);

		fis.close();
		assert complete != null;
		return complete.digest();
	}


}
