package code.weatherstation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/*
 * The Weatherstation class receives sensor data via UDP and handles Http requests via TCP.
 */
public class Weatherstation{

  private static final Logger log = Logger.getLogger( Weatherstation.class.getName() );

  private static InetAddress receiveIpAddress;
  private static int receivePort;

  private static InetAddress serverIpAddress;
  private static int serverPort;

  private static String locationId;
  private static String thriftServerIp;
  private static String thriftServerPort;
  private static String serverPortFile;
  private int fileRetries = 0;

  private String stationName;

  public Weatherstation(String n, String receiveIP, String receiveP, String serverIP, String serverP,
      String locId, String tServerIp, String tServerPort, String portFile) {
    this.stationName = n;
    locationId=locId;
    thriftServerIp=tServerIp;
    thriftServerPort= tServerPort;
    serverPortFile = portFile;


    try {
      receiveIpAddress = InetAddress.getByName(receiveIP);
      serverIpAddress = InetAddress.getByName(serverIP);
    } catch (UnknownHostException e) {
      System.out.println("Please enter a valid IP-Address");
    }
    receivePort = Integer.parseInt(receiveP);
    serverPort = Integer.parseInt(serverP);
  }

  private synchronized String getPort(String portFile) {
    String line;
    String returnPort = "";
    Map<String, Integer> serverPorts = new HashMap<>();
    ArrayList<Object[]> ports = new ArrayList<>();

    try {
      File file = new File(portFile);
      BufferedReader br = new BufferedReader(new FileReader(file));
      while ((line = br.readLine()) != null) {
        String[] portline = line.split(",");
        String port = portline[0];
        int connections = Integer.parseInt(portline[1]);

      }
    } catch (IOException e) {
      e.printStackTrace();
      System.out.println("Could not create or write to file");
      this.fileRetries++;
      if (this.fileRetries >= 3) {
        System.exit(1);
      } else {
        getPort(portFile);
      }
    }
    this.fileRetries = 0;

    String minPort = (String) ports.get(0)[0];
    int minConecctions = (Integer) ports.get(0)[1];

    return returnPort;
  }

  public static void main (String [] args)
  {
    Weatherstation weatherstation=
        new Weatherstation(args[0], args[1], args[2], args[3], args[4], args[5],args[6],args[7], args[8]);
    System.out.println(weatherstation.stationName);

    HttpServer httpServer = new HttpServer(serverPort, serverIpAddress, weatherstation.stationName);
    WStationThriftClient wStationThriftClient = new WStationThriftClient(thriftServerIp,
        thriftServerPort,
        weatherstation.stationName, locationId);
    SensorDataHandler sensorDataHandler = new SensorDataHandler(receivePort, receiveIpAddress,
        weatherstation.stationName,
        wStationThriftClient);


    //launch httpServer thread
    Thread serverThread= new Thread(httpServer);
    serverThread.start();

    //launch sensorDataHandler thread
    Thread sensorDataHandlerThread= new Thread(sensorDataHandler);
    sensorDataHandlerThread.start();
  }
}
