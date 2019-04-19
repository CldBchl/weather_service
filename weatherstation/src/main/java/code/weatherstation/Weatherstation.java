package code.weatherstation;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Logger;

/*
 * This is the Weatherstation class.
 */

public class Weatherstation{

  private static final Logger log = Logger.getLogger( Weatherstation.class.getName() );

  private static InetAddress receiveIpAddress;
  private static int receivePort;
  private static DatagramSocket udpSocket;
  private static SensorDataHandler sensorDataHandler;

  private static InetAddress serverIpAddress;
  private static int serverPort;
  private static HttpServer server;

  private String stationName;

  public static void main (String [] args)
  {
    Weatherstation myWS=
        new Weatherstation(args[0], args[1], args[2], args[3], args[4]);
    System.out.println(myWS.stationName);

    sensorDataHandler=new SensorDataHandler(receivePort, receiveIpAddress);
    server= new HttpServer(serverPort, serverIpAddress);

    sensorDataHandler.run();
    server.run();
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


}
