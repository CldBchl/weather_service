package code.weatherstation;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * The HttpServer class accepts http requests and assigns them to the HttpRequestHandler.
 */

public class HttpServer implements Runnable {

  private static final Logger log = Logger.getLogger(HttpServer.class.getName());
  private static InetAddress serverIpAddress;
  private static int serverPort;
  private static ServerSocket serverSocket;
  private static Socket server;
  private static int backlog = 1024;

  public HttpServer(int port, InetAddress ip) {
    try {
      serverIpAddress = ip;
      serverPort = port;
      //serverSocket accepts incoming requests and passes them to new httpRequestHandler
      serverSocket = new ServerSocket(serverPort, backlog, serverIpAddress);

      log.log(Level.INFO, "Successful serverSocket socket creation");
    } catch (IOException e) {
      e.printStackTrace();
      log.log(Level.WARNING, "Server socket initialization failed");
      //TODO: handle error
      return;

    }
  }

  private void handleRequests(){
    try {

    HttpRequestHandler handler;
    while (true)
    {
      //accept () accepts a connection that is made to this socket and creates a new socket
      handler = new HttpRequestHandler(serverSocket.accept());
      //launch new thread for handling the http request
      handler.start();
    }
    } catch (IOException e) {
      e.printStackTrace();
      log.log(Level.WARNING, "Error when starting new httpHandler thread");
    }

  }

  @Override
  public void run() {
    log.log(Level.INFO, "httpServer thread successful");
    handleRequests();
  }
}
