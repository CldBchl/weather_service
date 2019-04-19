package code.weatherstation;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;

public class HttpServer {

  private static ServerSocket server;
  private static int backlog =1024;

  public HttpServer(int p, InetAddress ip) throws IOException {
    server= new ServerSocket(p, backlog,ip);
    
  }

}
