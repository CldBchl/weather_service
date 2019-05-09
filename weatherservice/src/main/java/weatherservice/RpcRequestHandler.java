package weatherservice;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.nio.charset.StandardCharsets.UTF_8;

 /* The HttpRequestHandler class handles incoming http requests.
 */

public class RpcRequestHandler extends Thread {

    private static final Logger log = Logger.getLogger(RpcRequestHandler.class.getName());
    private static SocketChannel socketChannel;
    private static SelectionKey key;

    private String httpRequest;

    public RpcRequestHandler(SelectionKey k) {
        key = k;
        socketChannel = (SocketChannel) key.channel();
        log.log(Level.INFO, "Successful handlerSocket creation");
    }

    private void handleRequests() {
        Boolean connectionIsActive = readMessage();
        if (connectionIsActive) {
            processRequest(httpRequest);
            //set key to check readiness to read
            //key.interestOps(SelectionKey.OP_READ);
        }
    }

    @Override
    public void run() {
        handleRequests();
    }

    private Boolean readMessage() {
        try {
            ByteBuffer byteBuffer = (ByteBuffer) key.attachment();
            byteBuffer.clear();
            int bytesRead = socketChannel.read(byteBuffer);

            if (bytesRead > 0) {
                //flips buffer from read to write mode
                byteBuffer.flip();
                CharBuffer charBuffer = Charset.defaultCharset().newDecoder().decode(byteBuffer);

                String message = charBuffer.toString();

                String[] lines = message.split("\r\n");
                httpRequest = lines[0];

                return true;

            } else {
                //no message received --> close connection
                log.log(Level.WARNING, "Client closed connection");
                key.channel().close();
                key.cancel();
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
            log.log(Level.WARNING, "Http message could not be read");
            return null;
        }
    }

    private void processRequest(String request) {
        String[] tokens = request.split(" ");
        String method = tokens[0];
        String calledEndpoint = tokens[1];

        // only accept http: GET
        if (method.equals("GET")) {

        } else {

        }
    }

}

