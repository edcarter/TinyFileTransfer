import java.io.IOException;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

/**
 * A handler which is launched for each client connection
 */
class ConnectionHandler implements Runnable {

    private Socket sock;
    private Protocol p;
    private String fileDir;
    private Shadow shadow;

    /**
     * CTOR
     * @param sock socket connected to client
     * @param fileDir directory from where the requested files are fetched
     */
    ConnectionHandler(Socket sock, String fileDir) {
        this.sock = sock;
        this.fileDir = fileDir;
        this.shadow = new Shadow();
    }

    @Override
    public void run() {
        try {
            p = new Protocol(sock);

            // main message handling loop
            while (!sock.isClosed()) {
                ArrayList<Byte> buf = new ArrayList<>();
                String header = p.GetMessage(buf);
                switch (header) {
                    case Protocol.authenticatePrefix:
                        handleAuthentication(buf);
                        break;
                    case Protocol.fileRequestPrefix:
                        handleFileRequest(buf);
                        break;
                    case Protocol.finishMessage:
                        sock.close();
                        System.out.println("Connection finished by client");
                        return;
                }
            }
        } catch (Exception ex) { }
        System.out.println("Connection closed: " + sock.getRemoteSocketAddress());
    }

    /**
     * Handle authentication request from client
     * @param data
     */
    private void handleAuthentication(ArrayList<Byte> data) {
        byte[] buf = toByteArray(data);
        String str = new String(buf, Protocol.protocolEncoding);
        String userName = str.split(Protocol.authenticationSeparator)[0];
        String passWord = str.split(Protocol.authenticationSeparator)[1];
        String response;
        boolean validUser = shadow.Authenticate(userName, passWord);
        if (validUser) {
            response = "OK";
        } else {
            response = "ERR";
        }
        p.AuthenticationResponse(response);
        try {
            if (response.equals("ERR")) sock.close();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Handle file request from client
     * @param data
     */
    private void handleFileRequest(ArrayList<Byte> data) {
        byte[] buf = toByteArray(data);
        String location = fileDir + new String(buf, Protocol.protocolEncoding);
        Path path = Paths.get(location);
        if (!Files.exists(path)) {
            p.SendError("File not found: " + path);
            return;
        }
        try {
            if (Files.size(path) > Protocol.maxFileSize) {
                p.SendError("File too large, size greater than: " + Protocol.maxFileSize + "MB");
                return;
            }
            byte[] file = Files.readAllBytes(path);
            p.SendFile(file);
        } catch (IOException ex) {
            p.SendError("IOException when reading: " + path);
        }
    }

    /**
     * Helper method to convert an arrayList of Bytes
     * to a byte array
     * @param buf byte arrayList
     * @return byte array
     */
    private byte[] toByteArray(ArrayList<Byte> buf) {
        byte[] buf2 = new byte[buf.size()];
        for (int i = 0; i < buf.size(); i++) buf2[i] = buf.get(i);
        return buf2;
    }
}
