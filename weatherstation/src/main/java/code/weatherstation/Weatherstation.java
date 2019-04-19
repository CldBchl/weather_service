package code.weatherstation;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * This is the Weatherstation class.
 */

public class Weatherstation implements Runnable{

  private static final Logger log = Logger.getLogger( Weatherstation.class.getName() );

  private static InetAddress receiveIpAddress;
  private static int receivePort;
  private static DatagramSocket udpSocket;

  private static InetAddress serverIpAddress;
  private static int serverPort;

  private String stationName;

  public void main (String [] args)
  {
    Weatherstation myWS=
        new Weatherstation(args[0], args[1], args[2], args[3], args[4]);
    System.out.println(myWS.stationName);

    initializeSocket();
    //receiveUDPPackets();

    run();
  }

  public Weatherstation(String n, String receiveIP, String receiveP, String serverIP, String serverP) {
    stationName = n;
    try {
      receiveIpAddress = InetAddress.getByName(receiveIP);
      serverIpAddress = InetAddress.getByName(serverIP);
    } catch (UnknownHostException e) {
      System.out.println("Please enter a valid IP-Address");
    }
    receivePort = Integer.parseInt(receiveP);
    serverPort = Integer.parseInt(serverP);
  }

  private static void initializeSocket(){

    try {
      udpSocket = new DatagramSocket(receivePort, receiveIpAddress);
      udpSocket.setReceiveBufferSize(1024);
      log.log(Level.INFO, "succesful receive socket creation");
    } catch (SocketException e) {
      e.printStackTrace();
      log.log(Level.WARNING, "Receive socket initialization failed");
    }

  }

  private static void receiveUDPPackets(){
    byte[] buf = new byte[1024];
    DatagramPacket packet = new DatagramPacket(buf, 1024);

    //wait for incoming UDP package
    while (true) {
      try {

        udpSocket.receive(packet);

        // Empfänger auslesen
        InetAddress address = packet.getAddress();
        int         port    = packet.getPort();
        int         len     = packet.getLength();
        byte[]      data    = packet.getData();

        System.out.printf( "Anfrage von %s vom Port %d mit der Länge %d:%n%s%n",
            address, port, len, new String( data, 0, len ) );
      }
      catch (IOException e) {
        e.printStackTrace();
        System.out.println("Error when receiving UDP packet");
        log.log(Level.WARNING, "UDP error when receiving package");

      }
    }
  }

  private static void parseAndStoreSensorData(){

    //TODO: implement method
  }

  private static void initializeHttpServer(){

    try {
      HttpServer server= new HttpServer(serverPort, serverIpAddress);
      log.log(Level.INFO, "succesful receive socket creation");
    } catch (IOException e) {

      System.out.println("Error when receiving UDP packet");
      log.log(Level.WARNING, "Receive socket initialization failed");
    }


  }


  @Override
  public void run() {
  receiveUDPPackets();
  initializeHttpServer();
  }
}
