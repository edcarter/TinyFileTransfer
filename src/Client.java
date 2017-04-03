import java.io.IOException;
import java.net.Socket;

/**
 * Created by elias on 03/04/17.
 */
public class Client {
    public static void main(String[] args) {
        System.loadLibrary("tea");
        String serverName = args[0];
        int port = Integer.parseInt(args[1]);
        try {
            System.out.println("Connecting to " + serverName + " on port " + port);
            Socket client = new Socket(serverName, port);

            System.out.println("Just connected to " + client.getRemoteSocketAddress());
            Protocol p = new Protocol(client);
            p.AuthenticateUser("edcarter", "mypass");
            p.CloseSession();
            client.close();
        }catch(IOException e) {
            e.printStackTrace();
        }
    }
}
