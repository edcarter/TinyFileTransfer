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
        System.loadLibrary("tea"); //TODO
        String serverName = args[0];
        int port = Integer.parseInt(args[1]);
        try {
            System.out.println("Connecting to " + serverName + " on port " + port);
            Socket client = new Socket(serverName, port);

            System.out.println("Just connected to " + client.getRemoteSocketAddress());
            Protocol p = new Protocol(client);

            Scanner reader = new Scanner(System.in);
            System.out.println("Enter a username: ");
            String userName = reader.next();

            System.out.println("Password: ");
            String passWord = reader.next();

            p.AuthenticateUser(userName, passWord);
            ArrayList<Byte> buf = new ArrayList<>();
            String header = p.GetMessage(buf);
            byte[] bytes = toByteArray(buf);
            if ("ERR".equals(new String(bytes, StandardCharsets.UTF_8))) {
                System.out.println("Error authenticating.");
                return;
            }
            System.out.println("recieved: " + new String(bytes, StandardCharsets.UTF_8));

            while (true) {
                System.out.println("Enter file name or 'EXIT': ");
                String response = reader.next();
                if (response.equals("EXIT")) {
                    break;
                }
                p.RequestFile(response);
                buf = new ArrayList<>();
                header = p.GetMessage(buf);
                bytes = toByteArray(buf);
                System.out.println("recieved: " + new String(bytes, StandardCharsets.UTF_8));

                Path path = Paths.get(response);
                Files.write(path, bytes);
            }
            p.CloseSession();
            client.close();
        }catch(IOException e) {
            e.printStackTrace();
        }
    }

    private static byte[] toByteArray(ArrayList<Byte> buf) {
        byte[] buf2 = new byte[buf.size()];
        for (int i = 0; i < buf.size(); i++) buf2[i] = buf.get(i);
        return buf2;
    }
}
