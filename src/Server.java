import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Server main class
 */
public class Server {
    public static void main(String[] args) {
        int portNumber = Integer.parseInt(args[0]);
        String fileDirectory = args[1];
        Shadow s = new Shadow();
        createDummyShadow(s);

        // based off of https://docs.oracle.com/javase/tutorial/networking/sockets/clientServer.html
        try {
            ServerSocket serverSocket = new ServerSocket(portNumber);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Accepted connection from client: " + clientSocket.getRemoteSocketAddress());
                ConnectionHandler h = new ConnectionHandler(clientSocket, fileDirectory);
                Thread t = new Thread(h);
                t.start();
            }
        } catch (IOException ex) {
            System.out.println("IOException: " + ex.getMessage());
        }
    }

    private static void createDummyShadow(Shadow s) {
        s.EraseShadowFile();
        s.AddUser("edcarter", "mypass");
        s.AddUser("user2", "pass2");
    }
}
