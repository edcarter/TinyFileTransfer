import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Shadow password file manager
 */
public class Shadow {
    private static String shadowFile = "shadow";
    private static final String shadowFileDelim = "::";
    private static final long[] hashKey = new long[]{841609575, 503076106, 935458534, -550565543};

    public Shadow() {

    }

    /**
     * Authenticate that username exists in shadow file and
     * that he password and salt hash to the saved hash value.
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

    public void CreateDummyShadow() {
        String username = "edcarter";
        String pass = "mypass";
        String salt = "8sdfgjaksdfawasndfkasld";
        byte[] saltedPass = Salt(pass, salt);
        byte[] hashedPass = Hash(saltedPass, hashKey);
        String hashedPassString = new String(hashedPass, Protocol.protocolEncoding);
        String entry = username + shadowFileDelim + salt + shadowFileDelim + hashedPassString;

        Path path = Paths.get(shadowFile);
        try {
            Files.write(path, entry.getBytes(Protocol.protocolEncoding));
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
