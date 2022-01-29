/*
  User: Cloudy
  Date: 17/01/2022
  Time: 22:44
*/

package cz.cloudy.minecraft.core.hashing;

/**
 * @author Cloudy
 */
public record HashingAlgorithm(String name) {
    public static final HashingAlgorithm SHA1   = new HashingAlgorithm("SHA-1");
    public static final HashingAlgorithm SHA160 = SHA1;
    public static final HashingAlgorithm SHA256 = new HashingAlgorithm("SHA-256");
    public static final HashingAlgorithm SHA384 = new HashingAlgorithm("SHA-384");
    public static final HashingAlgorithm SHA512 = new HashingAlgorithm("SHA-512");
}
