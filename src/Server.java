import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * Created by elias on 30/03/17.
 */
public class Server {
    public static void main(String[] args) {
        byte[] b = "GGGGGGGGGGGGGGGG".getBytes(StandardCharsets.UTF_8);
        long[] k = new long[]{1,2,3,4};
        System.out.println("Before: "  + Arrays.toString(b));
        System.loadLibrary("tea");

        byte[] encrypted = TEA.encrypt(b, k);

        System.out.println("After: " + Arrays.toString(encrypted));
        byte[] decrypted = TEA.decrypt(encrypted, k);
        System.out.println("Finally: " + Arrays.toString(decrypted));
    }
}
