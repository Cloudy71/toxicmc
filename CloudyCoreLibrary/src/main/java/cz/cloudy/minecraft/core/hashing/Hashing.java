/*
  User: Cloudy
  Date: 17/01/2022
  Time: 22:44
*/

package cz.cloudy.minecraft.core.hashing;

import cz.cloudy.minecraft.core.LoggerFactory;
import cz.cloudy.minecraft.core.componentsystem.annotations.Component;
import org.slf4j.Logger;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author Cloudy
 */
@Component
public class Hashing {
    private static final Logger logger = LoggerFactory.getLogger(Hashing.class);

    private byte[] getBytes(String input, HashingAlgorithm algorithm, String key) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance(algorithm.name());
            messageDigest.update(key.getBytes(StandardCharsets.UTF_8));
            byte[] bytes = messageDigest.digest(input.getBytes(StandardCharsets.UTF_8));
            return bytes;
        } catch (NoSuchAlgorithmException e) {
            logger.error("", e);
            return null;
        }
    }

    public String hashStringChar(String input, HashingAlgorithm algorithm, String key) {
        byte[] bytes = getBytes(input, algorithm, key);
        if (bytes == null)
            return null;

        return new String(bytes, StandardCharsets.UTF_8);
    }


    public String hashStringHex(String input, HashingAlgorithm algorithm, String key) {
        byte[] bytes = getBytes(input, algorithm, key);
        if (bytes == null)
            return null;

        StringBuilder output = new StringBuilder();
        for (byte aByte : bytes) {
            output.append(Integer.toString((aByte & 0xFF) + 0x100, 16).substring(1));
        }
        return output.toString();
    }
}
