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
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * The HttpRequestHandler class handles incoming http requests.
 */

public class HttpRequestHandler extends Thread {

  static final File WEB_ROOT = new File("../../../Resources");
  static final String DEFAULT_FILE = "../../../Resources.SensorDataTemplate.html";
  static final String FILE_NOT_FOUND = "BadRequest.html";
  static final String METHOD_NOT_SUPPORTED = "BadRequest.html";

  private static final Logger log = Logger.getLogger(HttpRequestHandler.class.getName());
  private static Socket socket;
  private static SocketChannel socketChannel;
  private static SelectionKey key;

  private BufferedReader httpMessage;
  private String httpRequest;

  public HttpRequestHandler(SelectionKey k) {
    key=k;
    socketChannel=(SocketChannel)key.channel();
    socket = socketChannel.socket();
    log.log(Level.INFO, "Successful handlerSocket creation");
  }

  private void handleRequests() {
    log.log(Level.INFO, "enter HandlerSocket method");
    httpRequest =readMessage();

    //set key to check readiness to read
    key.interestOps(SelectionKey.OP_READ);
  }

  @Override
  public void run() {

    //System.out.println("in run method");
    handleRequests();

  }

  private String readMessage() {

    try {
      ByteBuffer byteBuffer= (ByteBuffer)key.attachment();
      byteBuffer.clear();
      socketChannel.read(byteBuffer);

      //flips buffer from read to write mode
      byteBuffer.flip();
      CharBuffer charBuffer = Charset.defaultCharset().newDecoder().decode(byteBuffer);

      String message=charBuffer.toString();
      System.out.println(message);
      return message;

    } catch (IOException e) {
      e.printStackTrace();
      log.log(Level.WARNING, "Http message could not be read");
      return  null;
    }
  }
/*
  private void processRequest(String request){
    String method;
    method=request.substring(0,2).toUpperCase();
    if (method.equals("GET")
    ) {
      try {
        OutputStream response= socket.getOutputStream();
        OutputStreamWriter outputStreamWriter= new OutputStreamWriter(response);

      } catch (IOException e) {
        e.printStackTrace();
      }

    }
    else{

      // we return the not supported file to the client
      File file = new File(WEB_ROOT, METHOD_NOT_SUPPORTED);
      int fileLength = (int) file.length();
      String contentMimeType = "text/html";

    }
    }
    */

}



