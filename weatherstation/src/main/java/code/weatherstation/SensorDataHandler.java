package code.weatherstation;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.*;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import java.io.FileWriter;

/*
 * The SensorDataHandler class receives data via UDP and processes the incoming messages.
 */

public class SensorDataHandler implements Runnable{

  private static final Logger log = Logger.getLogger( SensorDataHandler.class.getName() );
  private static DatagramSocket udpSocket;

  public SensorDataHandler(int receivePort, InetAddress receiveIpAddress){
    try {
      udpSocket = new DatagramSocket(receivePort, receiveIpAddress);
      udpSocket.setReceiveBufferSize(1024);
      log.log(Level.INFO, "Successful UDP socket creation");
    } catch (SocketException e) {
      e.printStackTrace();
      log.log(Level.WARNING, "UDP socket initialization failed");
      //TODO: handle error
      return;
    }

  }

  private static void handleSensorData(){
    while (true) {
      String data = receiveUDPPackets();
      parseAndStoreSensorData(data);
    }
  }


  private static String receiveUDPPackets(){
    byte[] buf = new byte[1024];
    DatagramPacket packet = new DatagramPacket(buf, 1024);

    //wait for incoming UDP package
    while (true) {
      try {

        udpSocket.receive(packet);

        InetAddress address = packet.getAddress();
        int         port    = packet.getPort();
        int         len     = packet.getLength();
        byte[]      data    = packet.getData();

        String dataString = new String( data, 0, len );

        //System.out.printf( "Receive data from IP %s and from port %d :%n%s%n",
        //    address, port, dataString);

        return dataString;

      }
      catch (IOException e) {
        e.printStackTrace();
        System.out.println("Error when receiving UDP packet");
        log.log(Level.WARNING, "Error when UDP receiving package");

        return "error";

      }

    }
  }

  private static void parseAndStoreSensorData(String data){
    //System.out.println(data);

    JSONParser parser = new JSONParser();
    try {
      Object obj = parser.parse(data);
      JSONObject json = (JSONObject) obj;
      System.out.println(json);
      json.get("unit");

     switch ((String) json.get("type")){
       case "tenperature":
         storeSensorData(json);
         break;

       case "rain":
         storeSensorData(json);
         break;

       case "wind":
         storeSensorData(json);
         break;

       case "humidity":
         storeSensorData(json);
         break;

         default:
           System.out.println("Invalid sensortype: " + json.get("type") );
     }


    }catch (ParseException e){
      e.printStackTrace();
    }

    //TODO: implement method
  }

  private static void storeSensorData(JSONObject jsonObject){
    try {
     FileWriter file = new FileWriter("./"+jsonObject.get("type"));
     file.write(jsonObject.toString());
    } catch (IOException e) {
      e.printStackTrace();
      System.out.println("Could not create or write to file");
    }
  }

  @Override
  public void run() {
    log.log(Level.INFO, "sensorDataHandler thread successful");
    handleSensorData();

  }
}
