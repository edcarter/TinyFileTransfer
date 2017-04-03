import javax.crypto.KeyAgreement;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;

/**
 * Created by elias on 03/04/17.
 */
public class SecretNegotiator {
    public static byte[] negotiateSecret(Socket sock) {
        try {
            OutputStream out = sock.getOutputStream();
            InputStream in = sock.getInputStream();
            KeyAgreement keyAgreement = KeyAgreement.getInstance(Protocol.keyExchangeAlgorithm);
            KeyPairGenerator keyGenerator = KeyPairGenerator.getInstance(Protocol.keyExchangeAlgorithm);
            keyGenerator.initialize(Protocol.keyExchangeHashSize);
            KeyPair pair = keyGenerator.generateKeyPair();
            keyAgreement.init(pair.getPrivate());
            byte[] ourPublic = pair.getPublic().getEncoded();
            byte[] outPublicLength = ByteBuffer.allocate(4).putInt(ourPublic.length).array();
            out.write(outPublicLength);
            out.write(ourPublic);
            int read = 0;
            byte[] lengthBuf = new byte[4];
            in.read(lengthBuf, 0, 4);
            int keySizeBytes = ByteBuffer.wrap(lengthBuf).getInt();
            byte[] keyBuf = new byte[keySizeBytes];
            while ((read = in.read(keyBuf, read, keySizeBytes-read)) != -1) {
                if (read == keySizeBytes) break;
            }
            PublicKey publicKey = KeyFactory.getInstance(Protocol.keyExchangeAlgorithm)
                                            .generatePublic(new X509EncodedKeySpec(keyBuf));
            keyAgreement.doPhase(publicKey, true);
            return keyAgreement.generateSecret();
        } catch (Exception ex) {
            System.out.println("Exception in negotiateSecret: " + ex.getMessage());
        }
        return null;
    }
}
