package code.weatherstation;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HttpServer implements Runnable{

  private static final Logger log = Logger.getLogger( HttpServer.class.getName() );
  private static ServerSocket server;
  private static int backlog =1024;

  static final File WEB_ROOT = new File("./Resources");
  static final String DEFAULT_FILE = "index.html";
  static final String FILE_NOT_FOUND = "404.html";
  static final String METHOD_NOT_SUPPORTED = "not_supported.html";

  public HttpServer(int p, InetAddress ip) {
    try {
      server= new ServerSocket(p, backlog,ip);
      log.log(Level.INFO, "Successful server socket creation");
    } catch (IOException e) {
      e.printStackTrace();
      log.log(Level.WARNING, "Server socket initialization failed");
    }
  }


  @Override
  public void run() {
    log.log(Level.INFO, "sensorDataHandler thread successful");
    //TODO: Call listen method here
  }
}
