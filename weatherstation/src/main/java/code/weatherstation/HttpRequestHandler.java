package code.weatherstation;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
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

  private static final Logger log = Logger.getLogger(HttpRequestHandler.class.getName());
  private static SocketChannel socketChannel;
  private static SelectionKey key;

  private String httpRequest;
  private int response202 = 202;
  private int response404 = 404;

  public HttpRequestHandler(SelectionKey k) {
    key = k;
    socketChannel = (SocketChannel) key.channel();
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
        byteBuffer.clear();
        log.log(Level.WARNING, "Server closed connection");
        key.channel().close();
        key.cancel();
      } catch (IOException e) {
        e.printStackTrace();
        //TODO handle error
      }
    } else {
      try {
        // we return the not supported file to the client

        //final Enumeration<URL> en = HttpRequestHandler.class.getClassLoader().getResources("");
        URL url = getClass().getClassLoader().getResource("BadRequest.html");

        try {
          File file = new File(url.toURI());

          String path = file.getPath();
          byte[] byteArrayBody = Files.readAllBytes(Paths.get(path));

          int bodyLenght = byteArrayBody.length;

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
          log.log(Level.WARNING, "Server closed connection");
          key.channel().close();
          key.cancel();
        } catch (URISyntaxException e) {
          e.printStackTrace();
        }
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
    header += "Content-type:text/html\r\n";
    header += "Content-length:" + lenght;
    header += "\r\n\r\n";
    return header;
  }


}



