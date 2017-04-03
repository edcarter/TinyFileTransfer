import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

/**
 * Created by elias on 02/04/17.
 */
public class Protocol {

    public static final String keyExchangeAlgorithm = "DiffieHellman";
    public static final int    keyExchangeHashSize = 1024;

    private static final String fileRequestPrefix = "file:";
    private static final String errorPrefix = "error:";
    private static final String finishMessage = "finish:finish";
    private static final String authenticatePrefix = "auth:";

    private static final String authenticationSeparator = "jasdfj2104812hdfasoduf:a;sdlfj";

    private Socket sock;
    private long[] sharedKey;

    public Protocol(Socket sock) {
        this.sock = sock;
        byte[] bSharedKey = SecretNegotiator.negotiateSecret(sock);
        // convert to long array
        ByteBuffer byteBuffer = ByteBuffer.wrap(bSharedKey);
        LongBuffer longBuffer = byteBuffer.asLongBuffer();
        sharedKey = new long[longBuffer.capacity()];
        longBuffer.get(sharedKey);
    }

    public void AuthenticateUser(String userName, String password) {
        String message = authenticatePrefix + userName + authenticationSeparator + password;
        SendEncryptedData(message.getBytes(StandardCharsets.UTF_8));
    }

    public void RequestFile(String filePath) {
        String message = fileRequestPrefix + filePath;
        SendEncryptedData(message.getBytes(StandardCharsets.UTF_8));
    }

    public void SendError(String error) {
        String message = errorPrefix + error;
        SendEncryptedData(message.getBytes(StandardCharsets.UTF_8));
    }

    public void SendFile(byte[] file) {
        byte[] encodedPrefix = fileRequestPrefix.getBytes(StandardCharsets.UTF_8);
        byte[] data = new byte[encodedPrefix.length + file.length];
        for (int i = 0; i < data.length; i++) {
            if (i < encodedPrefix.length) data[i] = encodedPrefix[i];
            else data[i] = file[i - encodedPrefix.length];
        }
    }

    // Fill message buffer and return the header
    public String GetMessage(ArrayList<Byte> message) {
        byte[] data = ReadData();
        byte separator = ':';
        int separatorIndex = 0;
        for (separatorIndex = 0; data[separatorIndex] != separator; separatorIndex++) {}
        byte[] header = new byte[separatorIndex];

        // copy header bytes into array
        for (int i = 0; i < separatorIndex; i++) header[i] = data[i];
        String h = new String(header, StandardCharsets.UTF_8);

        // copy message
        for (int i = separatorIndex + 1; i < data.length; i++) message.add(data[i]);
        return h;
    }

    // Read Length Header and then return byte buffer containing
    // the number of bytes specified in the length header
    private byte[] ReadData() {
        try {
            InputStream in = sock.getInputStream();
            byte[] lengthBuf = new byte[4];
            in.read(lengthBuf, 0, 4);
            int dataLength = ByteBuffer.wrap(lengthBuf).getInt();
            byte[] buf = new byte[dataLength];
            int read = 0;

            while ((read = in.read(buf, read, dataLength-read)) != -1) {
                if (read == dataLength) break;
            }
            return buf;
        } catch (IOException ex) {}
        return null;
    }

    public void CloseSession() {
        SendEncryptedData(finishMessage.getBytes(StandardCharsets.UTF_8));
    }

    private void SendEncryptedData(byte[] bytes) {
        // append message length header
        byte[] messageLength = ByteBuffer.allocate(4).putInt(bytes.length).array();
        byte[] both = new byte[messageLength.length + bytes.length];
        TEA.encrypt(bytes, sharedKey);
        for (int i = 0; i<both.length; i++) {
            if (i < messageLength.length) both[i] = messageLength[i]; // copy message length
            else both[i] = bytes[i-messageLength.length]; // otherwise copy message
        }
        try {
            sock.getOutputStream().write(both);
        } catch (IOException ex) {}
    }
}
