import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Scanner;

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
            Socket sock = new Socket(serverName, port);

            System.out.println("Just connected to " + sock.getRemoteSocketAddress());
            Protocol p = new Protocol(sock);

            Scanner reader = new Scanner(System.in);
            System.out.println("Enter a username: ");
            String userName = reader.next();

            System.out.println("Password: ");
            String passWord = reader.next();

            p.AuthenticateUser(userName, passWord);

            String response = null;
            while (!sock.isClosed()) {
                ArrayList<Byte> buf = new ArrayList<>();
                String header = p.GetMessage(buf);
                byte[] data = toByteArray(buf);

                switch (header) {
                    case Protocol.authenticatePrefix:
                        String authenticationResponse = new String(data, StandardCharsets.UTF_8);
                        if (authenticationResponse.equals("ERR")) {
                            System.out.println("Error authenticating");
                            return;
                        }
                        break;
                    case Protocol.fileRequestPrefix:
                        Path path = Paths.get(response);
                        Files.write(path, data);
                        break;
                    case Protocol.finishMessage:
                        System.out.println("Server terminated connection");
                        return;
                    case Protocol.errorPrefix:
                        String errorMessage = new String(data, StandardCharsets.UTF_8);
                        System.out.println("Error from server: " + errorMessage);
                        break;
                }
                System.out.println("Enter file name or 'EXIT': ");
                response = reader.next();
                if (response.equals("EXIT")) {
                    break;
                }
                p.RequestFile(response);
            }
            p.CloseSession();
            sock.close();
        }catch(IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private static byte[] toByteArray(ArrayList<Byte> buf) {
        byte[] buf2 = new byte[buf.size()];
        for (int i = 0; i < buf.size(); i++) buf2[i] = buf.get(i);
        return buf2;
    }
}
