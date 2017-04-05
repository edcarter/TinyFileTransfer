import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Created by elias on 04/04/17.
 */
public class Shadow {
    private static String shadowFile = "shadow";
    private static final String shadowFileDelim = "::";
    private static final long[] hashKey = new long[]{841609575, 503076106, 935458534, -550565543};

    public Shadow() {

    }

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
                return VerifyHash(hash, new String(computedHash, StandardCharsets.UTF_8));
            }
        } catch (IOException ex) { }
        return false;
    }

    public void CreateDummyShadow() {
        String username = "edcarter";
        String pass = "mypass";
        String salt = "8sdfgjaksdfawasndfkasld";
        byte[] saltedPass = Salt(pass, salt);
        byte[] hashedPass = Hash(saltedPass, hashKey);
        String hashedPassString = new String(hashedPass, StandardCharsets.UTF_8);
        String entry = username + shadowFileDelim + salt + shadowFileDelim + hashedPassString;

        Path path = Paths.get(shadowFile);
        try {
            Files.write(path, entry.getBytes(StandardCharsets.UTF_8));
        } catch (Exception ex) {}
    }

    private byte[] Salt(String s, String salt) {
        byte[] sB = s.getBytes(StandardCharsets.UTF_8);
        byte[] saltB = salt.getBytes(StandardCharsets.UTF_8);
        for (int i = 0; i < sB.length; i++) {
            sB[i] = (byte)(sB[i] ^ saltB[i % saltB.length]);
        }
        return sB;
    }

    private byte[] Hash(byte[] b, long[] key) {
        return TEA.encrypt(b,key);
    }

    private boolean VerifyHash(String correct, String computed) {
        return correct.equals(computed);
    }

    private void PrintBytes(String prefix, byte[] bytes) {
        System.out.println(prefix);
        for (byte b : bytes) System.out.println(b);
    }
}
