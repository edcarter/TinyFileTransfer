import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

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
            Protocol p = new Protocol(sock);
            ArrayList<Byte> buf = new ArrayList<>();
            String header = p.GetMessage(buf);
            byte[] buf2 = new byte[buf.size()];
            for (int i = 0; i < buf.size(); i++) buf2[i] = buf.get(i);
            System.out.println("Recieved: " + new String(buf2, StandardCharsets.UTF_8));
        } catch (Exception ex) { }
    }
}
