import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

/**
 * Protocol for communicating over an encrypted socket
 */
class Protocol {

    /**
     * Message Headers
     */
    static final String fileRequestPrefix = "file:";
    static final String errorPrefix = "error:";
    static final String finishMessage = "finish:finish";
    static final String authenticatePrefix = "auth:";

    /**
     * Separator for username and password in authentication method
     */
    static final String authenticationSeparator = "::";

    /**
     * 100 MB max file size, since we are loading
     * whole files into memory, it is unwise to try to load
     * 100+ MB files into memory
     */
    static final int maxFileSize = 100000000;

    static final Charset protocolEncoding = StandardCharsets.UTF_8;

    private Socket sock;
    private long[] sharedKey;

    Protocol(Socket sock) {
        this.sock = sock;

        // get the shared key and convert it to a long[4] array
        byte[] bSharedKey = SecretNegotiator.negotiateSecret(sock);
        ByteBuffer byteBuffer = ByteBuffer.wrap(bSharedKey);
        LongBuffer longBuffer = byteBuffer.asLongBuffer();
        long[] longKey = new long[longBuffer.capacity()];
        longBuffer.get(longKey);
        sharedKey = new long[4];
        sharedKey[0] = longKey[0];
        sharedKey[1] = longKey[1];
        sharedKey[2] = longKey[2];
        sharedKey[3] = longKey[3];
    }

    /**
     * Send authentication information to server
     * @param userName
     * @param password
     */
    void AuthenticateUser(String userName, String password) {
        String message = authenticatePrefix + userName + authenticationSeparator + password;
        SendEncryptedData(message.getBytes(protocolEncoding));
    }

    /**
     * Send file request to server
     * @param filePath
     */
    void RequestFile(String filePath) {
        String message = fileRequestPrefix + filePath;
        SendEncryptedData(message.getBytes(protocolEncoding));
    }

    /**
     * Send error message to other party
     * @param error
     */
    void SendError(String error) {
        String message = errorPrefix + error;
        SendEncryptedData(message.getBytes(protocolEncoding));
    }

    /**
     * Send file to client
     * @param file
     */
    void SendFile(byte[] file) {
        byte[] encodedPrefix = fileRequestPrefix.getBytes(protocolEncoding);
        byte[] data = new byte[encodedPrefix.length + file.length];
        for (int i = 0; i < data.length; i++) {
            if (i < encodedPrefix.length) data[i] = encodedPrefix[i];
            else data[i] = file[i - encodedPrefix.length];
        }
        SendEncryptedData(data);
    }

    /**
     * Send authentication response to client
     * @param response
     */
    void AuthenticationResponse(String response) {
        String message = authenticatePrefix + response;
        SendEncryptedData(message.getBytes(protocolEncoding));
    }

    /**
     * Send a close session message to other party
     */
    public void CloseSession() {
        SendEncryptedData(finishMessage.getBytes(protocolEncoding));
    }

    /**
     * Get a message from the other party
     * @param message buffer to fill message data with
     * @return message header
     */
    String GetMessage(ArrayList<Byte> message) {
        byte[] data = ReadData();
        byte separator = ':';
        int separatorIndex;
        for (separatorIndex = 0; data[separatorIndex] != separator; separatorIndex++) {}
        byte[] header = new byte[separatorIndex+1];
        // copy header bytes into array
        for (int i = 0; i <= separatorIndex; i++) header[i] = data[i];
        String h = new String(header, protocolEncoding);
        // copy message
        for (int i = separatorIndex + 1; i < data.length; i++) message.add(data[i]);
        return h;
    }

    /**
     * Get unencrypted raw message byte data from other party
     * @return
     */
    private byte[] ReadData() {
        try {
            InputStream in = sock.getInputStream();
            byte[] lengthBuf = new byte[4];
            in.read(lengthBuf, 0, 4);
            int dataLength = ByteBuffer.wrap(lengthBuf).getInt();
            byte[] buf = new byte[dataLength];
            int read = 0;

            while ((read += in.read(buf, read, dataLength-read)) != -1) {
                if (read == dataLength) break;
            }
            return TEA.decrypt(buf, sharedKey);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Encrypt bytes and then send it to the other party
     * @param bytes message to encrypt and send
     */
    private void SendEncryptedData(byte[] bytes) {
        byte[] encrypted = TEA.encrypt(bytes, sharedKey);
        byte[] messageLength = ByteBuffer.allocate(4).putInt(encrypted.length).array();
        byte[] both = new byte[messageLength.length + encrypted.length];

        for (int i = 0; i<both.length; i++) {
            if (i < messageLength.length) both[i] = messageLength[i]; // copy message length
            else both[i] = encrypted[i-messageLength.length]; // otherwise copy message
        }
        try {
            sock.getOutputStream().write(both);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}
