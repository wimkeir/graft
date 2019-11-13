package graft.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import graft.GraftException;

public class FileUtil {

    // TODO: logging and error handling

    private static Logger log = LoggerFactory.getLogger(FileUtil.class);

    private static final int BUFFER_SIZE = 8192;
    private static final String HASH_ALGORITHM = "SHA-256";

    /**
     * Hash the contents of a file.
     *
     * @param file the file to hash
     * @return the hash of the file
     * @throws GraftException if the operation fails
     */
    public static String hashFile(File file) throws GraftException {
        // see https://stackoverflow.com/a/32032908/6208351
        byte[] buffer = new byte[BUFFER_SIZE];
        int count;

        try {
            MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
            BufferedInputStream in = new BufferedInputStream(new FileInputStream(file));
            while ((count = in.read(buffer)) > 0) {
                digest.update(buffer, 0, count);
            }
            in.close();
            byte[] hash = digest.digest();
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException | IOException e) {
            throw new GraftException("Could not hash file '" + file.getName() + "'", e);
        }
    }

    /**
     * Get the full name of the Java class in a given class file, relative to the given package
     * root directory.
     *
     * @param rootDir the package root directory
     * @param classFile the class file
     * @return the full Java class name
     */
    public static String getClassName(File rootDir, File classFile) {
        return rootDir.toURI()
                .relativize(classFile.toURI())
                .getPath()
                .replace('/', '.')
                .replace(".class", "");
    }
}
