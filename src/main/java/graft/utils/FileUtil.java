package graft.utils;

import graft.GraftException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class FileUtil {

    private static Logger log = LoggerFactory.getLogger(FileUtil.class);

    private static final int BUFFER_SIZE = 8192;
    private static final String HASH_ALGORITHM = "SHA-256";

    // see https://stackoverflow.com/a/32032908/6208351
    public static String hashFile(File file) {
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
        } catch (NoSuchAlgorithmException e) {
            log.error("Invalid hash algorithm", e);
        } catch (FileNotFoundException e) {
            log.error("Could not find file '{}' to hash", file.getName(), e);
        } catch (IOException e) {
            log.error("Could not read file '{}' to hash", file.getName(), e);
        }

        throw new GraftException("Could not hash file '" + file.getName() + "'");
    }

    public static String getClassName(File rootDir, File classFile) {
        return rootDir.toURI()
                .relativize(classFile.toURI())
                .getPath()
                .replace('/', '.')
                .replace(".class", "");
    }
}
