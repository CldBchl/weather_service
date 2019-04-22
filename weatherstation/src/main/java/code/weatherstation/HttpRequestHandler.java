package code.weatherstation;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;
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
  private static InetAddress socketIP;
  private static int socketPort;
  private static Socket socket;
  private String httpDelimiter = "\r\n\r\n";

  private BufferedReader httpMessage;
  private String httpRequest;

  public HttpRequestHandler(Socket s) {

    socket = s;
    log.log(Level.INFO, "Successful handlerSocket creation");
  }

  private void handleRequests() {
    log.log(Level.INFO, "enter HandlerSocket method");
    httpRequest =readMessage();

  }

  public Socket getSocket(){
      return this.socket;
  }

  @Override
  public void run() {

    handleRequests();
  }

  private String readMessage() {
    try {
      Scanner scanner = new Scanner(new InputStreamReader(socket.getInputStream()));
      scanner.useDelimiter(httpDelimiter);
      String message;
      message = scanner.next();
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



