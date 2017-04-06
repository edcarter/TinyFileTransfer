/**
 * Tiny Encryption Algorithm
 */
public class TEA {

    static {
        // load shared library libtea.so for native encryption methods
        System.loadLibrary("tea");
    }

    /**
     * Number of bytes that can be encrypted at a time
     */
    private static int blockSize = 16;

    /**
     * Encrypt a byte array using a long array as the key
     * @param bytes bytes to encrypt
     * @param key key to use in encryption, must be at least 4 longs in length.
     *            Only first 4 longs are used in encryption.
     * @return encrypted bytes which may be padded to a multiple of blockSize
     */
    public static byte[] encrypt(byte[] bytes, long[] key) {
        byte[] padded = pad(bytes);
        _encrypt(padded, key);
        return padded;
    }

    /**
     * Decrypt a potentially padded byte array using a long array as the key
     * @param bytes potentially padded byte array, must be a multiple of blockSize in length
     * @param key key to use in encryption, must be at least 4 longs in length.
     *            Only first 4 longs are used in encryption.
     * @return un-encrypted and un-padded bytes
     */
    public static byte[] decrypt(byte[] bytes, long[] key) {
        _decrypt(bytes, key);
        byte[] unpadded = unpad(bytes);
        return unpadded;
    }

    /**
     * Pad bytes using ISO/IEC 9797-1 padding method 2.
     * Add a bit '1' after message, and then pad 0's to get desired length.
     * @param bytes bytes to pad
     * @return padded bytes which are a multiple of blockSize
     */
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

    /**
     * Un-pad bytes using ISO/IEC 9797-1 padding method 2.
     * @param bytes bytes which are a multiple of blockSize to un-pad.
     * @return un-padded bytes
     */
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

    /**
     * Native method for TEA encryption
     * @param bytes bytes which are multiple of blockSize to encrypt
     * @param key key to use in encryption, must be at least 4 longs in length.
     *            Only first 4 longs are used in encryption.
     */
    private static native void _encrypt(byte[] bytes, long[] key);

    /**
     * Native method for TEA decryption
     * @param bytes bytes which are multiple of blockSize to decrypt
     * @param key key to use in decryption, must be at least 4 longs in length.
     *            Only first 4 longs are used in decryption.
     */
    private static native void _decrypt(byte[] bytes, long[] key);
}
