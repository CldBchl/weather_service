package code.weatherstation;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HttpServer implements Runnable{

  private static ServerSocket server;
  private static int backlog =1024;

  private static final Logger log = Logger.getLogger( HttpServer.class.getName() );

  public HttpServer(int p, InetAddress ip) {
    try {
      server= new ServerSocket(p, backlog,ip);
      log.log(Level.INFO, "successful receive socket creation");
    } catch (IOException e) {
      e.printStackTrace();
      log.log(Level.WARNING, "Receive socket initialization failed");
    }

  }


  @Override
  public void run() {
    log.log(Level.INFO, "sensorDataHandler thread successful");

  }
}
