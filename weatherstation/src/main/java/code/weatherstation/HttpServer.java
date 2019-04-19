package code.weatherstation;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;

public class HttpServer implements Runnable{

  private static ServerSocket server;
  private static int backlog =1024;

  public HttpServer(int p, InetAddress ip) throws IOException {
    server= new ServerSocket(p, backlog,ip);
    
  }


  @Override
  public void run() {
    System.out.println("thread2");
  }
}
