/**
 * Created by elias on 30/03/17.
 */
public class TEA {
    private static int blockSize = 16;

    public static byte[] encrypt(byte[] bytes, long[] key) {
        byte[] padded = pad(bytes);
        _encrypt(padded, key);
        return padded;
    }

    public static byte[] decrypt(byte[] bytes, long[] key) {
        _decrypt(bytes, key);
        byte[] unpadded = unpad(bytes);
        return unpadded;
    }

    // pad bytes using ISO/IEC 9797-1 padding method 2
    // add a bit '1' after message, and then pad 0's to get desired length
    private static byte[] pad(byte[] bytes) {
        int length = bytes.length;
        int remainder = length % blockSize;
        byte[] padded = bytes;
        if (remainder != 0) {
            int toPad = blockSize - remainder;
            padded = new byte[length + toPad];
            System.arraycopy(bytes, 0, padded, 0, length);
            padded[length] = 8; // pad '10000000'
            for (int i = 1; i < toPad; i++) {
                padded[length + i] = 0; // pad '00000000'
            }
        }
        return padded;
    }

    private static byte[] unpad(byte[] bytes) {
        int index = bytes.length - 1;
        for ( ; index > 0; index--) {
            byte b = bytes[index];
            if (b == 8) break; // we found the padding byte
            if (b != 0) return bytes; // the message isn't padded
        }
        int unpaddedLength = index;
        byte[] unpadded = new byte[unpaddedLength];
        System.arraycopy(bytes, 0, unpadded, 0, unpaddedLength);
        return unpadded;
    }

    private static native void _encrypt(byte[] bytes, long[] key);
    private static native void _decrypt(byte[] bytes, long[] key);
}
