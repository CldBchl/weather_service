package code.weatherstation;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * The HttpRequestHandler class handles incoming http requests.
 */

public class HttpRequestHandler extends Thread {

  static final File WEB_ROOT = new File("/Resources/BadRequest.html");
  static final String DEFAULT_FILE = "../../../Resources.SensorDataTemplate.html";
  static final String FILE_NOT_FOUND = "BadRequest.html";
  static final String METHOD_NOT_SUPPORTED = "BadRequest.html";

  private static final Logger log = Logger.getLogger(HttpRequestHandler.class.getName());
  private static Socket socket;
  private static SocketChannel socketChannel;
  private static SelectionKey key;

  private BufferedReader httpMessage;
  private String httpRequest;
  private int response202 = 202;
  private int response404 = 404;

  public HttpRequestHandler(SelectionKey k) {
    key = k;
    socketChannel = (SocketChannel) key.channel();
    socket = socketChannel.socket();
    log.log(Level.INFO, "Successful handlerSocket creation");
  }

  private void handleRequests() {
    log.log(Level.INFO, "enter HandlerSocket method");
    Boolean connectionIsActive = readMessage();
    if (connectionIsActive) {
      processRequest(httpRequest);
      //set key to check readiness to read
      key.interestOps(SelectionKey.OP_READ);
    }

  }

  @Override
  public void run() {

    //System.out.println("in run method");
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
        System.out.println(message);

        String[] lines = message.split("\r\n");
        httpRequest = lines[0];

        return true;
      } else {
        //no meassage received --> close connection
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

    if (method.equals("GET")) {

      log.log(Level.WARNING, "Received a get request");
      String body;
      body = "<!DOCTYPE html>";
      body += "<html>";
      body += "<head>";
      body += "<meta charset=\"UTF-8\">";
      body += "<title>Sensor data</title>";
      body += "</head>";
      body += "<body>";
      body += "<h1>Sensordaten</h1>";
      body += "</body>";
      body += "</html>";

      int bodyLength = body.length();

      ByteBuffer byteBuffer = (ByteBuffer) key.attachment();
      byteBuffer.clear();

      String header = buildHttpHeader(response202, bodyLength);

      String httpResponse = header + body;
      byte[] byteArray = httpResponse.getBytes(Charset.defaultCharset());
      byteBuffer = byteBuffer.put(byteArray);
      byteBuffer.flip();
      try {
        socketChannel.write(byteBuffer);
      } catch (IOException e) {
        e.printStackTrace();
        //TODO handle error
      }
      byteBuffer.clear();
    } else {
      try {
        // we return the not supported file to the client
        File file = new File(WEB_ROOT, METHOD_NOT_SUPPORTED);
        String path = WEB_ROOT.getPath();
        byte[] byteArrayBody = Files.readAllBytes(Paths.get(path));

        int bodyLenght = (int) byteArrayBody.length;

        String header = buildHttpHeader(response404, bodyLenght);

        ByteBuffer byteBuffer = (ByteBuffer) key.attachment();
        byteBuffer.clear();
        //String httpResponse = header + body;
        byte[] byteArrayHeader = header.getBytes(Charset.defaultCharset());
        byteBuffer = byteBuffer.put(byteArrayHeader);
        byteBuffer = byteBuffer.put(byteArrayBody);

        byteBuffer.flip();

        socketChannel.write(byteBuffer);
        byteBuffer.clear();
      } catch (IOException e) {
        e.printStackTrace();
        //TODO handle error
      }

    }
  }

  private String buildHttpHeader(int code, int lenght) {
    String header;

    if (code == response202) {
      header = "HTTP/1.1 200 Ok\r\n";
    } else {
      header = "HTTP/1.1 404 Not Found\r\n";
    }
    header += "Content-type: text/html\r\n";
    header += "Content-length: " + lenght;
    header += "\r\n\r\n";
    return header;
  }


}



