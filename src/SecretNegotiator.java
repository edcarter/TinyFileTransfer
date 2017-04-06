import javax.crypto.KeyAgreement;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;

/**
 * Negotiate shared secrets using Diffie-Hellman
 */
public class SecretNegotiator {

    private static final String keyExchangeAlgorithm = "DiffieHellman";
    private static final int    keyExchangeHashSize = 1024;

    /**
     * Negotiate a shared secret over the socket using Diffie-Hellman.
     * Each party on either end of the socket should call this method.
     * @param sock socket used in Diffie-Hellman exchange
     * @return shared key in bytes
     */
    public static byte[] negotiateSecret(Socket sock) {
        try {
            OutputStream out = sock.getOutputStream();
            InputStream in = sock.getInputStream();
            KeyAgreement keyAgreement = KeyAgreement.getInstance(keyExchangeAlgorithm);
            KeyPairGenerator keyGenerator = KeyPairGenerator.getInstance(keyExchangeAlgorithm);
            keyGenerator.initialize(keyExchangeHashSize);
            KeyPair pair = keyGenerator.generateKeyPair();
            keyAgreement.init(pair.getPrivate());
            byte[] ourPublic = pair.getPublic().getEncoded();
            byte[] ourPublicLength = ByteBuffer.allocate(4).putInt(ourPublic.length).array();
            out.write(ourPublicLength);
            out.write(ourPublic);
            int read = 0;
            byte[] lengthBuf = new byte[4];
            in.read(lengthBuf, 0, 4);
            int keySizeBytes = ByteBuffer.wrap(lengthBuf).getInt();
            byte[] keyBuf = new byte[keySizeBytes];
            while ((read = in.read(keyBuf, read, keySizeBytes-read)) != -1) {
                if (read == keySizeBytes) break;
            }
            PublicKey publicKey = KeyFactory.getInstance(keyExchangeAlgorithm)
                                            .generatePublic(new X509EncodedKeySpec(keyBuf));
            keyAgreement.doPhase(publicKey, true);
            return keyAgreement.generateSecret();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
