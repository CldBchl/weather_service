package code.weatherstation;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.input.ReversedLinesFileReader;

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
  private String weatherstation;

  public HttpRequestHandler(SelectionKey k, String weatherstationName) {
    key = k;
    weatherstation = weatherstationName;
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
    String calledEndpoint =tokens[1];

    // only accept http: GET
    if (method.equals("GET")){
      resolveGet(calledEndpoint);
    } else {
      rejectRequest();
    }
  }

  private void resolveGet(String calledEndpoint) {
    log.log(Level.INFO, "Received a get request");

    // built response body
    String responseBody;
    responseBody  = httpHeader();
    responseBody += httpBody(getDataFromEndpoint(calledEndpoint));
    responseBody += httpFooter();

    // built header with ok response with correct header length
    String header = buildHttpHeader(response202, responseBody.getBytes().length);

    // convert strings to bytes
    byte[] byteArrayBody = responseBody.getBytes(UTF_8);
    byte[] byteArrayHeader = header.getBytes(Charset.defaultCharset());

    // initiate buffer
    ByteBuffer byteBuffer = ByteBuffer.allocate(byteArrayHeader.length + byteArrayBody.length);
    byteBuffer.clear();

    // write complete response into buffer
    byteBuffer = byteBuffer.put(byteArrayHeader);
    byteBuffer = byteBuffer.put(byteArrayBody);
    byteBuffer.flip();

    try {
      // send byteBuffer
      while (byteBuffer.hasRemaining()) {
        socketChannel.write(byteBuffer);
      }

      // clean up and close connection
      byteBuffer.clear();
      log.log(Level.INFO, "Server closed connection");
      key.channel().close();
      key.cancel();
    } catch (IOException e) {
      e.printStackTrace();
      log.log(Level.WARNING, "Error when sending 200 response");
    }
  }

  private void rejectRequest() {
    try {
      // return "404 not found" to client

      //read BadRequest.html into a ByteArrayOutputStream and parse it to byteBuffer
      String fileContent= readFileToString("BadRequest.html");
      byte[] byteArrayBody = fileContent.getBytes(UTF_8);
      int bodyLength = fileContent.length();
      String header = buildHttpHeader(response404, bodyLength);

      //get selector-key's byte buffer
      ByteBuffer byteBuffer = (ByteBuffer) key.attachment();
      byteBuffer.clear();
      byte[] byteArrayHeader = header.getBytes(Charset.defaultCharset());

      //add header and body to byteBuffer
      byteBuffer = byteBuffer.put(byteArrayHeader);
      byteBuffer = byteBuffer.put(byteArrayBody);

      byteBuffer.flip();
      socketChannel.write(byteBuffer);
      byteBuffer.clear();

      log.log(Level.INFO, "Server closed connection");
      key.channel().close();
      key.cancel();
    } catch (IOException e) {
      e.printStackTrace();
      log.log(Level.WARNING, "Error when sending 404 response");
    }
  }

  private String httpHeader() {
    return    "<!DOCTYPE html>\n"
            + "<html>\n"
            + "<head>\n"
            + "\t<meta charset=\"UTF-8\">\n"
            + "\t<title>Sensor data</title>\n"
            + "</head>\n";
  }

  private String httpBody(String bodyData){
    return    "<body>\n"
            + bodyData
            + "</body>\n";
  }

  private String httpFooter(){
    return "</html>";
  }

  private String getDataFromEndpoint(String calledEndpoint) {
    switch (calledEndpoint){

      case "/sensors/temperature/current":
        return getSensorCurrent("temperature");

      case "/sensors/wind/current":
        return getSensorCurrent("wind");

      case "/sensors/rain/current":
        return getSensorCurrent("rain");

      case "/sensors/humidity/current":
        return getSensorCurrent("humidity");

      case "/sensors/temperature/history":
        return getSensorHistory("temperature");

      case "/sensors/wind/history":
        return getSensorHistory("wind");

      case "/sensors/rain/history":
        return getSensorHistory("rain");

      case "/sensors/humidity/history":
        return  getSensorHistory("humidity");
      case "/sensors/all/current":
        return  getSensorCurrent("temperature") +
                getSensorCurrent("wind") +
                getSensorCurrent("rain") +
                getSensorCurrent("humidity");

      case "/sensors/all/history":
        return  getSensorHistory("temperature") +
                getSensorHistory("wind") +
                getSensorHistory("rain") +
                getSensorHistory("humidity");

      default:
        return "no valid endpoint";
    }
  }

  private String getSensorCurrent(String sensorType){
    StringBuilder sensorData = new StringBuilder();

    try{
      File file = new File("./sensorData/" + weatherstation + "/" + sensorType+ ".txt");
      ReversedLinesFileReader rf = new ReversedLinesFileReader(file, UTF_8);

      sensorData.append(rf.readLine());
      sensorData.append(System.lineSeparator());

      return sensorData.toString();

    } catch (IOException e) {
      e.printStackTrace();
      System.out.println("Could not create or write to file");
      return "No file";
    }
  }

  private String getSensorHistory(String sensorType){
    StringBuilder sensorData = new StringBuilder();
    String line;

    try{
      File file = new File("./sensorData/" + weatherstation + "/" + sensorType+ ".txt");
      BufferedReader br = new BufferedReader(new FileReader(file));

      while ((line = br.readLine()) != null){
        sensorData.append(line);
        sensorData.append(System.lineSeparator());
      }

      return sensorData.toString();

  } catch (IOException e) {
    e.printStackTrace();
    System.out.println("Could not create or write to file");
    return "No file";
  }

}


  private String buildHttpHeader(int code, int length) {
    String header;
    String delimiter = "\r\n\r\n";

    if (code == response202) {
      header = "HTTP/1.1 200 OK\r\n";
    } else {
      header = "HTTP/1.1 404 Not Found\r\n";
    }
    header += "Content-Type: text/html charset=utf-8\r\n";
    header += "Content-Length: " + length;
    header += delimiter;
    return header;
  }

  private String readFileToString( String fileName) throws IOException {
    InputStream in = getClass().getClassLoader().getResourceAsStream("BadRequest.html");
    ByteArrayOutputStream fileContent = new ByteArrayOutputStream();
    int length;
    byte[] buffer = new byte[1024];

    while ((length = in.read(buffer)) != -1) {
      fileContent.write(buffer, 0, length);
    }

    return fileContent.toString(UTF_8.name());
  }
}