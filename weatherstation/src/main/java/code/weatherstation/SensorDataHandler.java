package code.weatherstation;

import code.weatherstation.thrift.Weather;
import code.weatherstation.thrift.WeatherReport;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence;
import org.json.JSONObject;

/*
 * The SensorDataHandler class receives data via MQTT/UDP and processes the incoming messages.
 */

public class SensorDataHandler implements Runnable {

  private static final Logger log = Logger.getLogger(SensorDataHandler.class.getName());
  private static DatagramSocket udpSocket;
  private String stationName;
  private static WStationThriftClient weatherClient;
  private static ExecutorService executorService;
  private WeatherReport report;

  public SensorDataHandler(int receivePort, InetAddress receiveIpAddress, String stationName,
      WStationThriftClient client){
    this.stationName = stationName;
    weatherClient = client;
    report= new WeatherReport();
    //the executorService manages as single WStationThriftClient thread and queues submitted tasks
    executorService= Executors.newSingleThreadExecutor();
    new File("./temp/paho").mkdirs();
    MqttDefaultFilePersistence filePersistence =
            new MqttDefaultFilePersistence("./temp/paho");

    try {
      MqttClient mqttClient=new MqttClient("tcp://localhost:1883",
              MqttClient.generateClientId(),filePersistence);
      mqttClient.setCallback( new SimpleMqttCallback(this, client) );
      mqttClient.connect();
      mqttClient.subscribe(String.valueOf(receivePort));
    } catch (MqttException e) {
      e.printStackTrace();
    }


    /*try {
      udpSocket = new DatagramSocket(null);
      udpSocket.setReuseAddress(true);
      udpSocket.bind(new InetSocketAddress(receiveIpAddress, receivePort));
      udpSocket.setReceiveBufferSize(1024);
      log.log(Level.INFO, "Successful UDP socket creation");
    } catch (IOException e) {
      e.printStackTrace();
      log.log(Level.WARNING, "UDP socket initialization failed");

    }*/

  }

  private void handleSensorData() {
    while (true) {
      //String data = receiveUDPPackets();
      //parseAndStoreSensorData(data);
      //prepareWeatherReport(data);

      //executes the run method of WStationThriftClient or queues the request
      //if our single thread is currently working
      //executorService.execute(weatherClient);
    }
  }


  /*private static String receiveUDPPackets() {
    byte[] buf = new byte[1024];
    DatagramPacket packet = new DatagramPacket(buf, 1024);

    //wait for incoming UDP package
    while (true) {
      try {

        udpSocket.receive(packet);

        InetAddress address = packet.getAddress();
        int port = packet.getPort();
        int len = packet.getLength();
        byte[] data = packet.getData();

        String dataString = new String(data, 0, len);
        //System.out.printf("Receive data from IP %s and from port %d :%n%s%n", address, port, dataString);

        return dataString;

      } catch (IOException e) {
        e.printStackTrace();
        System.out.println("Error when receiving UDP packet");
        log.log(Level.WARNING, "Error when UDP receiving package");

        return "error";

      }
    }
  }*/

  public void parseAndStoreSensorData(String data) {
    //System.out.println(data);

    JSONObject json = new JSONObject(data);
    // System.out.println(json.toString());

    switch ((String) json.get("type")) {
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
        System.out.println("Invalid sensortype: " + json.get("type"));
    }
  }

  private void storeSensorData(JSONObject jsonObject) {
    try {
      new File("./sensorData/" + stationName).mkdirs();
      FileWriter file = new FileWriter(
          "./sensorData/" + stationName + "/" + jsonObject.get("type") + ".txt", true);
      //jsonObject.put("station", stationName);
      file.append(jsonObject.toString());
      file.append("\n");
      file.flush();
    } catch (IOException e) {
      e.printStackTrace();
      System.out.println("Could not create or write to file");
    }
  }

  public void prepareWeatherReport(String data) {

    JSONObject json = new JSONObject(data);

    switch ((String) json.get("type")) {
      case "temperature":
        String temperatureAsString = json.getString("value");
        temperatureAsString = temperatureAsString.replace(",", ".");
        report.setTemperature(Double.parseDouble(temperatureAsString));
        break;

      case "rain":
        String rainAsString = json.getString("value");
        rainAsString = rainAsString.replace(",", ".");
        report.setRainfall(Double.parseDouble(rainAsString));
        break;

      case "wind":
        String windAsString = json.getString("value");
        windAsString = windAsString.replace(",", ".");
        windAsString = String.valueOf(Double.valueOf(windAsString).intValue());
        report.setWindStrength(Byte.parseByte(windAsString));
        break;

      case "humidity":
        String humidityAsString = json.getString("value");
        humidityAsString = humidityAsString.replace(",", ".");
        humidityAsString = String.valueOf(Double.valueOf(humidityAsString).intValue());
        report.setHumidity(Byte.parseByte(humidityAsString));
        break;

      default:
        System.out.println("Invalid sensortype: " + json.get("type"));
    }

    weatherClient.updateWeatherReport(report);

  }


  @Override
  public void run() {
    log.log(Level.INFO, "sensorDataHandler thread successful");
    //handleSensorData();
    while (true){ /* Running Loop */ }
  }
}

 class SimpleMqttCallback implements org.eclipse.paho.client.mqttv3.MqttCallback{

  private SensorDataHandler handler;
  private WStationThriftClient client;
  private ExecutorService executorService;

   public SimpleMqttCallback(SensorDataHandler handler, WStationThriftClient client){
     this.handler = handler;
     this.client = client;
     this.executorService= Executors.newSingleThreadExecutor();
   }

  public void connectionLost(Throwable throwable) {
    System.out.println("Connection to MQTT broker lost!");
  }

  public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
    System.out.println("Message received:\n\t"+ new String(mqttMessage.getPayload()) );

    String data = new String(mqttMessage.getPayload());
    handler.parseAndStoreSensorData(data);
    handler.prepareWeatherReport(data);
    executorService.execute(client);
  }

  public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
    // not used in this example
  }

}
