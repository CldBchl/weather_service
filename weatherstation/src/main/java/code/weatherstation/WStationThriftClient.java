package code.weatherstation;


import code.weatherstation.thrift.Location;
import code.weatherstation.thrift.Weather;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.transport.THttpClient;
import org.apache.thrift.transport.TTransportException;


public class WStationThriftClient {

  private static final Logger log = Logger.getLogger(WStationThriftClient.class.getName());

  //private TTransport transport;
  private static THttpClient transport;
  private String stationName;
  private TBinaryProtocol protocol;
  private static Weather.Client weatherClient;
  private static byte locationId = 0x3;
  private Location location;
  private static long userId;

  public WStationThriftClient(String serverIP, String serverPort, String sName) {

    stationName = sName;
    String url = "http://" + serverIP + ":" + serverPort + "/weather";
    try {
      transport = new THttpClient(url);
    } catch (TTransportException e) {
      e.printStackTrace();
    }
    protocol = new TBinaryProtocol(transport);
    weatherClient = new Weather.Client(protocol);

    //register shutDownTask which is executed when program exits
    ShutDownTask shutDownTask = new ShutDownTask();
    Runtime.getRuntime().addShutdownHook(shutDownTask);

    transport.open();
    location = new Location(locationId, stationName, 23.24, 45.45);

    Integer i = 0;
    Boolean succesfulLogin = false;
    while (!succesfulLogin) {
      succesfulLogin = performLogin(weatherClient);
      System.out.println("attempt " + i);
      i++;
    }
  }


  private Boolean performLogin(Weather.Client client) {
    try {
      userId = client.login(location);
      log.log(Level.INFO, "Login successful");
      locationId++; //increase id for the next object
      //return true;
    } catch (TException e) {
      e.printStackTrace();
      log.log(Level.WARNING, "Error at login");
      return false;
    }
    return true;
  }





  //private class which ensures a proper logout and closing of thrift after program exit
  private static class ShutDownTask extends Thread {

    @Override
    public void run() {
      System.out.println("Performing shutdown");
      try {
        Boolean successfulLogout = weatherClient.logout(userId);
        if (successfulLogout) {
          log.log(Level.INFO, "Logout successful");
        }
      } catch (TException e) {
        e.printStackTrace();
        log.log(Level.WARNING, "Error at login/logout");
      }
      transport.close();
    }
  }

}

