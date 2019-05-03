package code.weatherstation;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
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
  private static InetAddress serverIpAddress;
  private static int serverPort;
  private static ServerSocket serverSocket;
  private static Socket server;
  private static int backlog = 1024;
  private static Selector selector;
  private String weatherstation;

  public HttpServer(int port, InetAddress ip, String weatherstationName) {
    try {
      weatherstation = weatherstationName;
      serverIpAddress = ip;
      serverPort = port;

      //serverSocket accepts incoming requests and passes them to new httpRequestHandler
      //  serverSocket = new ServerSocket(serverPort, backlog, serverIpAddress);
      log.log(Level.INFO, "Successful serverSocket socket creation");

      selector = Selector.open();
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
      InetSocketAddress socketAddress = new InetSocketAddress(serverIpAddress, serverPort);
      serverSocket.bind(socketAddress);

      serverChannel.configureBlocking(false);

      selector = Selector.open();

      SelectionKey acceptKey = serverChannel.register(selector, SelectionKey.OP_ACCEPT);

      while (true) {
        //updates list of selected keys (=list of ready sockets)
        int events = selector.select();

        Set<SelectionKey> selectedKeys = selector.selectedKeys();

        Iterator<SelectionKey> keyIterator = selectedKeys.iterator();

        while(keyIterator.hasNext()) {

          SelectionKey key = keyIterator.next();

          if (key.isValid()) {
            if (key.isAcceptable()) {
              //add new client socket to watchlist
              acceptRequest(key);
            }
            if (key.isReadable()) {
              //remove interest in read readiness option while channel is serviced
              key.interestOps(key.interestOps(  ) & (~SelectionKey.OP_READ));
              HttpRequestHandler handler;
              handler = new HttpRequestHandler(key, weatherstation);
              //launch new thread for handling the http request
              handler.start();
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
      ///TODO: is write notify necessary?
      client.register(selector, SelectionKey.OP_READ, buffer);

      int status = key.interestOps();

    } catch (IOException e) {
      e.printStackTrace();
      log.log(Level.WARNING, "Error accepting connection");
    }

  }


}
