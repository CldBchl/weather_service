package code.weatherstation;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Logger;

/*
 * The Weatherstation class receives sensor data via UDP and handles Http requests via TCP.
 */

public class Weatherstation{

  private static final Logger log = Logger.getLogger( Weatherstation.class.getName() );

  private static InetAddress receiveIpAddress;
  private static int receivePort;
  private static DatagramSocket udpSocket;
  private static SensorDataHandler sensorDataHandler;

  private static InetAddress serverIpAddress;
  private static int serverPort;
  private static HttpServer httpServer;

  private static String locationId;
  private static String thriftServerIp; //141.100.70.110
  private static String thriftServerPort; //8080
  private static WStationThriftClient wStationThriftClient;


  private String stationName;

  public Weatherstation(String n, String receiveIP, String receiveP, String serverIP, String serverP,
      String locId, String tServerIp, String tServerPort) {
    this.stationName = n;
    locationId=locId;
    thriftServerIp=tServerIp;
    thriftServerPort=tServerPort;
    try {
      receiveIpAddress = InetAddress.getByName(receiveIP);
      serverIpAddress = InetAddress.getByName(serverIP);
    } catch (UnknownHostException e) {
      System.out.println("Please enter a valid IP-Address");
    }
    receivePort = Integer.parseInt(receiveP);
    serverPort = Integer.parseInt(serverP);
  }

  public static void main (String [] args)
  {
    Weatherstation weatherstation=
        new Weatherstation(args[0], args[1], args[2], args[3], args[4], args[5],args[6],args[7]);
    System.out.println(weatherstation.stationName);

    httpServer = new HttpServer(serverPort, serverIpAddress, weatherstation.stationName);
    wStationThriftClient= new WStationThriftClient(thriftServerIp, thriftServerPort,
        weatherstation.stationName, locationId);
    sensorDataHandler=new SensorDataHandler(receivePort, receiveIpAddress, weatherstation.stationName, wStationThriftClient);


    //launch httpServer thread
    Thread serverThread= new Thread(httpServer);
    serverThread.start();

    //launch sensorDataHandler thread
    Thread sensorDataHandlerThread= new Thread(sensorDataHandler);
    sensorDataHandlerThread.start();

  }


}
