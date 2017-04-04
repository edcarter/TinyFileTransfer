import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

/**
 * Created by elias on 02/04/17.
 */
class ConnectionHandler implements Runnable {

    private Socket sock;
    private Protocol p;
    private String fileDir;
    private Shadow shadow;


    ConnectionHandler(Socket sock, String fileDir) {
        this.sock = sock;
        this.fileDir = fileDir;
        this.shadow = new Shadow();
    }

    @Override
    public void run() {
        try {
            p = new Protocol(sock);
            while (!sock.isClosed()) {
                ArrayList<Byte> buf = new ArrayList<>();
                String header = p.GetMessage(buf);
                System.out.println(header);
                switch (header) {
                    case Protocol.authenticatePrefix:
                        handleAuthentication(buf);
                        break;
                    case Protocol.fileRequestPrefix:
                        handleFileRequest(buf);
                        break;
                    case Protocol.finishMessage:
                        sock.close();
                        return;
                }
            }
        } catch (Exception ex) { }
    }

    private void handleAuthentication(ArrayList<Byte> data) {
        byte[] buf = toByteArray(data);
        String str = new String(buf, StandardCharsets.UTF_8);
        String userName = str.split(Protocol.authenticationSeparator)[0];
        String passWord = str.split(Protocol.authenticationSeparator)[1];
        String response;
        boolean validUser = shadow.Authenticate(userName, passWord);
        if (validUser) {
            response = "OK";
        } else {
            response = "ERR";
        }
        /*if (userName.equals("edcarter") && passWord.equals("mypass")) {
            response = "OK";
        } else {
            response = "ERR";
        }*/
        p.AuthenticationResponse(response);
        try {
            if (response.equals("ERR")) sock.close();
        } catch (IOException ex) {}
    }

    private void handleFileRequest(ArrayList<Byte> data) {
        byte[] buf = toByteArray(data);
        String location = fileDir + new String(buf, StandardCharsets.UTF_8);
        Path path = Paths.get(location);
        if (!Files.exists(path)) {
            p.SendError("File not found: " + path);
        }
        try {
            byte[] file = Files.readAllBytes(path);
            p.SendFile(file);
        } catch (IOException ex) {
            p.SendError("IOException when reading: " + path);
        }
    }

    private byte[] toByteArray(ArrayList<Byte> buf) {
        byte[] buf2 = new byte[buf.size()];
        for (int i = 0; i < buf.size(); i++) buf2[i] = buf.get(i);
        return buf2;
    }
}
