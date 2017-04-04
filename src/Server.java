import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by elias on 30/03/17.
 */
public class Server {
    public static void main(String[] args) {
        //System.loadLibrary("tea"); //TODO
        int portNumber = Integer.parseInt(args[0]);
        String fileDirectory = args[1];

        // based off of https://docs.oracle.com/javase/tutorial/networking/sockets/clientServer.html
        try {
            ServerSocket serverSocket = new ServerSocket(portNumber);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("accepted connection from client");
                ConnectionHandler h = new ConnectionHandler(clientSocket, fileDirectory);
                Thread t = new Thread(h);
                t.start();
            }
        } catch (IOException ex) {
            System.out.println("IOException: " + ex.getMessage());
        }
    }
}
