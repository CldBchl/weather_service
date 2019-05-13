package code.weatherstation;

import code.weatherstation.thrift.WeatherReport;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONObject;

/*
 * The SensorDataHandler class receives data via UDP and processes the incoming messages.
 */

public class SensorDataHandler implements Runnable{

  private static final Logger log = Logger.getLogger( SensorDataHandler.class.getName() );
  private static DatagramSocket udpSocket;
  private String stationName;
  private static WStationThriftClient weatherClient;

  public SensorDataHandler(int receivePort, InetAddress receiveIpAddress, String stationName, WStationThriftClient client){
    this.stationName = stationName;
    this.weatherClient =client;
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

  private  void handleSensorData(){
    while (true) {
      String data = receiveUDPPackets();
      parseAndStoreSensorData(data);
      prepareWeatherReport(data);
      //Thread thread = new Thread(weatherClient);
      //thread.start();
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
        System.out.printf( "Receive data from IP %s and from port %d :%n%s%n", address, port, dataString);

        return  dataString;

      }
      catch (IOException e) {
        e.printStackTrace();
        System.out.println("Error when receiving UDP packet");
        log.log(Level.WARNING, "Error when UDP receiving package");

        return "error";

      }

    }
  }

  private void parseAndStoreSensorData(String data){
    //System.out.println(data);

      JSONObject json = new JSONObject(data);
     // System.out.println(json.toString());


     switch ((String) json.get("type")){
       case "temperature":
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
  }

  private  void storeSensorData(JSONObject jsonObject){
    try {
     new File("./sensorData/"+stationName).mkdirs();
     FileWriter file = new FileWriter("./sensorData/" + stationName + "/" + jsonObject.get("type")+ ".txt",true);
     //jsonObject.put("station", stationName);
     file.append(jsonObject.toString());
     file.append("\n");
     file.flush();
    } catch (IOException e) {
      e.printStackTrace();
      System.out.println("Could not create or write to file");
    }
  }

  private void prepareWeatherReport(String data){

    JSONObject json = new JSONObject(data);
    WeatherReport report= new WeatherReport();

    switch ((String) json.get("type")){
      case "temperature":
        String temperatureAsString=json.getString("value");
        temperatureAsString= temperatureAsString.replace(",",".");
        System.out.println("temperature value: " + temperatureAsString );
        report.setTemperature(Double.parseDouble(temperatureAsString));
        break;

      case "rain":
        String rainAsString=json.getString("value");
        rainAsString= rainAsString.replace(",",".");
        System.out.println("rain value: " + rainAsString );
        report.setRainfall(Double.parseDouble(rainAsString));
        break;

      case "wind":
        String windAsString=json.getString("value");
        windAsString= windAsString.replace(",",".");
        windAsString = String.valueOf(Double.valueOf(windAsString).intValue());
        System.out.println("wind value: " + windAsString );
        report.setWindStrength(Byte.parseByte(windAsString));
        break;

      case "humidity":
        String humidityAsString= json.getString("value");
        humidityAsString= humidityAsString.replace(",",".");
        humidityAsString = String.valueOf(Double.valueOf(humidityAsString).intValue());
        System.out.println("humidiy value: " + humidityAsString );
        report.setHumidity(Byte.parseByte(humidityAsString));
        break;

      default:
        System.out.println("Invalid sensortype: " + json.get("type") );
    }

    weatherClient.updateWeatherReport(report);

  }


  @Override
  public void run() {
    log.log(Level.INFO, "sensorDataHandler thread successful");
    handleSensorData();

  }
}
