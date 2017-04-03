import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.Key;
import java.net.Socket;

/**
 * Created by elias on 03/04/17.
 */
public class Client {
    public static void main(String[] args) {
        String serverName = args[0];
        int port = Integer.parseInt(args[1]);
        try {
            System.out.println("Connecting to " + serverName + " on port " + port);
            Socket client = new Socket(serverName, port);

            System.out.println("Just connected to " + client.getRemoteSocketAddress());
            byte[] secret = SecretNegotiator.negotiateSecret(client);
            System.out.println(new String(secret));
            client.close();
        }catch(IOException e) {
            e.printStackTrace();
        }
    }
}
