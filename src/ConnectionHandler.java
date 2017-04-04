import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.io.File;

/**
 * Created by elias on 02/04/17.
 */
class ConnectionHandler implements Runnable {

    Socket sock;
    Protocol p;
    String fileDir = "/home/elias/TinyFileTransfer/src/";


    ConnectionHandler(Socket sock) {
        this.sock = sock;
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
        if (userName.equals("edcarter") && passWord.equals("mypass")) {
            response = "OK";
        } else {
            response = "ERR";
        }
        p.AuthenticationResponse(response);
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
