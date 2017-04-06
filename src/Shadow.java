import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.SecureRandom;
import java.util.List;

/**
 * Shadow password file manager
 */
public class Shadow {
    private static String shadowFile = "shadow";
    private static final String shadowFileDelim = "::";
    private static final long[] hashKey = new long[]{841609575, 503076106, 935458534, -550565543};
    private static final int saltSize = 130; // 130 bits
    private SecureRandom saltGenerator;


    public Shadow() {
        saltGenerator = new SecureRandom();
    }

    /**
     * Authenticate that username exists in shadow file and
     * that the password and salt hash to the saved hash value.
     * @param userName
     * @param passWord
     * @return whether user is authenticated
     */
    public boolean Authenticate(String userName, String passWord) {
        try {
            Path shadowPath = Paths.get(shadowFile);
            List<String> lines = Files.readAllLines(shadowPath);
            for (String line : lines) {
                String username = line.split(shadowFileDelim)[0];
                String salt = line.split(shadowFileDelim)[1];
                String hash = line.split(shadowFileDelim)[2];
                if (!username.equals(userName)) continue; // check that we have entry for our user

                byte[] saltedPassword = Salt(passWord, salt);
                byte[] computedHash = Hash(saltedPassword, hashKey);
                return VerifyHash(hash, new String(computedHash, Protocol.protocolEncoding));
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        return false;
    }

    /**
     * Add user to shadow file
     * @param username user's name
     * @param pass user's password
     */
    public void AddUser(String username, String pass) {
        String salt = new BigInteger(saltSize, saltGenerator).toString(32 /* base 32 encoding */);
        byte[] saltedPass = Salt(pass, salt);
        byte[] hashedPass = Hash(saltedPass, hashKey);
        String hashedPassString = new String(hashedPass, Protocol.protocolEncoding);
        String entry = username + shadowFileDelim + salt + shadowFileDelim + hashedPassString + '\n';

        Path path = Paths.get(shadowFile);
        try {
            if (!Files.exists(path)) Files.createFile(path);
            Files.write(path, entry.getBytes(Protocol.protocolEncoding), StandardOpenOption.APPEND);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Erase the shadow file
     */
    public void EraseShadowFile() {
        Path path = Paths.get(shadowFile);
        try {
            if (Files.exists(path)) Files.delete(path);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Salt string s using the salt
     * @param s string to salt
     * @param salt value to use as salt
     * @return salted value
     */
    private byte[] Salt(String s, String salt) {
        byte[] sB = s.getBytes(Protocol.protocolEncoding);
        byte[] saltB = salt.getBytes(Protocol.protocolEncoding);
        for (int i = 0; i < sB.length; i++) {
            sB[i] = (byte)(sB[i] ^ saltB[i % saltB.length]);
        }
        return sB;
    }

    /**
     * Hash bytes using a key
     * @param b value to hash
     * @param key key to use in hashing function
     * @return hashed value
     */
    private byte[] Hash(byte[] b, long[] key) {
        return TEA.encrypt(b,key);
    }

    /**
     * Verify that the two hashed values are equivalent
     * @param correct the know correct hash value
     * @param computed the computed hash value in question
     * @return whether both hashes are equal
     */
    private boolean VerifyHash(String correct, String computed) {
        return correct.equals(computed);
    }
}
