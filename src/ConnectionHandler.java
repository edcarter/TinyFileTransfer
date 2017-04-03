import javax.crypto.KeyAgreement;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;

/**
 * Created by elias on 02/04/17.
 */
class ConnectionHandler implements Runnable {

    Socket sock;

    ConnectionHandler(Socket sock) {
        this.sock = sock;
    }

    @Override
    public void run() {
        try {
            byte[] secret = SecretNegotiator.negotiateSecret(sock);
            System.out.println(new String(secret));
            /*
            PrintWriter out = new PrintWriter(sock.getOutputStream(), true);
            InputStream in = sock.getInputStream();
            while (!sock.isClosed()) {
                String encrypted = in.read;
                String
                String prefix = message.split(":")[0];
                String postfix = message.split(":")[1];
                switch (prefix) {
                    case ProtocolConstants.finishMessage:
                        return;
                    case ProtocolConstants.fileRequestPrefix:
                        System.out.println()
                }
            }*/
        } catch (Exception ex) {
        }
    }
}
