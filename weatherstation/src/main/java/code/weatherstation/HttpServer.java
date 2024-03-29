package code.weatherstation;

import org.apache.thrift.TException;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * The HttpServer class accepts http requests and assigns them to the HttpRequestHandler.
 */

public class HttpServer implements Runnable {

  private static final Logger log = Logger.getLogger(HttpServer.class.getName());
  private InetAddress serverIpAddress;
  private int serverPort;
  private ServerSocket serverSocket;
  private int backlog = 50;
  private Selector selector;
  private String weatherstation;

  public HttpServer(int port, InetAddress ip, String weatherstationName) {
    try {
      weatherstation = weatherstationName;
      serverIpAddress = ip;
      serverPort = port;

      //serverSocket accepts incoming requests and passes them to new httpRequestHandler
      log.log(Level.INFO, "Successful serverSocket socket creation");

      selector = Selector.open();

      ShutDownTask shutDownTask = new ShutDownTask();
      Runtime.getRuntime().addShutdownHook(shutDownTask);

    } catch (IOException e) {
      e.printStackTrace();
      log.log(Level.WARNING, "Server socket initialization failed");
      //TODO: handle error
      return;

    }
  }

  private void manageRequests() {
    try {

      //initialize NIO Selector and add serversocket to watchlist

      ServerSocketChannel serverChannel = ServerSocketChannel.open();
      serverSocket = serverChannel.socket();
      serverSocket.setReuseAddress(true);
      InetSocketAddress socketAddress = new InetSocketAddress(serverIpAddress, serverPort);
      serverSocket.bind(socketAddress, backlog);

      serverChannel.configureBlocking(false);

      selector = Selector.open();

      serverChannel.register(selector, SelectionKey.OP_ACCEPT);



      while (true) {
        //updates list of selected keys (=list of ready sockets)
       selector.select();
       if (!selector.isOpen()){
         break;
       }

        Set<SelectionKey> selectedKeys = selector.selectedKeys();

        Iterator<SelectionKey> keyIterator = selectedKeys.iterator();

        while (keyIterator.hasNext()) {

          SelectionKey key = keyIterator.next();

          if (key.isValid()) {
            if (key.isAcceptable()) {
              //add new client socket to watchlist
              acceptRequest(key);
            }
            if (key.isReadable()) {
              //remove interest in read readiness option while channel is serviced
              key.interestOps(key.interestOps() & (~SelectionKey.OP_READ));

              //launch new thread for handling the http request
              Runnable runnable = new HttpRequestHandler(key, weatherstation);
              Thread thread = new Thread(runnable);
              thread.start();
            }
          }

          keyIterator.remove(); //remove socket from watchlist while it is being processed
        }
      }
      //TODO: close Selector at some point
    } catch (IOException e) {
      e.printStackTrace();
      log.log(Level.WARNING, "Error in selector initialization");
    }
  }


  @Override
  public void run() {
    log.log(Level.INFO, "httpServer thread successful");
    manageRequests();
  }

  public void acceptRequest(SelectionKey key) {

    try {
      ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();

      SocketChannel client = serverSocketChannel.accept();
      client.configureBlocking(false);
      System.out.println("Accepted connection with client" + client);

      ByteBuffer buffer = ByteBuffer.allocate(1024);

      client.register(selector, SelectionKey.OP_READ, buffer);

    } catch (IOException e) {
      e.printStackTrace();
      log.log(Level.WARNING, "Error accepting connection");
    }
  }

  private class ShutDownTask extends Thread {

    @Override
    public void run() {
      System.out.println("Performing http-shutdown : " + weatherstation);

      try {
        selector.close();
      } catch (IOException e) {
        e.printStackTrace();
        System.exit(2);
      }
    }
  }
}
