package code.weatherstation;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * The HttpRequestHandler class handles incoming http requests.
 */

public class HttpRequestHandler extends Thread {

  static final File WEB_ROOT = new File("../../../Resources");
  static final String DEFAULT_FILE = "../../../Resources.SensorDataTemplate.html";
  static final String FILE_NOT_FOUND = "404.html";
  static final String METHOD_NOT_SUPPORTED = "not_supported.html";

  private static final Logger log = Logger.getLogger(HttpRequestHandler.class.getName());
  private static InetAddress socketIP;
  private static int socketPort;
  private static Socket socket;

  public HttpRequestHandler(Socket s) {

    socket = s;
    log.log(Level.INFO, "Successful handlerSocket creation");
  }

  private void handleRequests() {
    log.log(Level.INFO, "enter HandlerSocket method");
    readMessage();
  }


  @Override
  public void run() {

    handleRequests();
  }

  public void readMessage() {
    try {
      BufferedReader httpMessage = new BufferedReader(
          new InputStreamReader(socket.getInputStream()));
    } catch (IOException e) {
      e.printStackTrace();
      log.log(Level.WARNING, "Error when reading http message");
    }
  }
}

