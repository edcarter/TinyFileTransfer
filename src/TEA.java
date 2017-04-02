/**
 * Created by elias on 30/03/17.
 */
public class TEA {
    public static native void encrypt(byte[] bytes, long[] key);
    public static native void decrypt(byte[] bytes, long[] key);
}
