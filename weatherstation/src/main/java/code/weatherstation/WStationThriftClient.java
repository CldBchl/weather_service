package code.weatherstation;


import code.weatherstation.thrift.Location;
import code.weatherstation.thrift.Weather;
import code.weatherstation.thrift.WeatherReport;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.transport.THttpClient;
import org.apache.thrift.transport.TTransportException;


public class WStationThriftClient implements Runnable {

  private static final Logger log = Logger.getLogger(WStationThriftClient.class.getName());

  //private TTransport transport;
  private static THttpClient transport;
  private String stationName;
  private TBinaryProtocol protocol;
  private static Weather.Client weatherClient;
  private static byte locationId = 0x8;
  private Location location;
  private static long userId;
  private static Boolean successfulLogin = false;
  private static WeatherReport weatherReport;

  public WStationThriftClient(String serverIP, String serverPort, String sName, String lId) {

    stationName = sName;
    locationId=Byte.parseByte(lId);
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

    weatherReport = new WeatherReport();
    weatherReport.setLocation(location);

    performLogin(weatherClient);


  }

  @Override
  public void run() {
    if (performSendWeatherReport(weatherClient)) {
      log.log(Level.INFO, "Weather report sent successfully");
    }
  }

  private Boolean performLogin(Weather.Client client) {
    try {
      userId = client.login(location);
      System.out.println(locationId);
      log.log(Level.INFO, "Thrift Login successful");
      locationId++; //increase id for the next object

    } catch (TException e) {
      e.printStackTrace();
      log.log(Level.WARNING, "Error at login");
      return false;
    }
    successfulLogin = true;
    return true;
  }

  private Boolean performSendWeatherReport(Weather.Client client) {

    try {
      return weatherClient.sendWeatherReport(weatherReport, userId);
    } catch (TException e) {
      e.printStackTrace();
      log.log(Level.WARNING, "Weather report could not be sent");
      return false;
    }
  }


  public void updateWeatherReport(WeatherReport update) {

    //update report with current sensor values
    weatherReport.setTemperature(update.getTemperature());
    weatherReport.setRainfall(update.getRainfall());
    weatherReport.setHumidity(update.getHumidity());
    weatherReport.setWindDirection(update.getWindStrength());

    //set current time
    String currentTimeISO8601 = ZonedDateTime.now(ZoneId.of("Europe/Paris"))
        .truncatedTo(ChronoUnit.SECONDS)
        .format(DateTimeFormatter.ISO_DATE_TIME);
    currentTimeISO8601 = currentTimeISO8601.replace("[Europe/Paris]", "");
    weatherReport.setDateTime((currentTimeISO8601));

    if (performSendWeatherReport(weatherClient)) {
      log.log(Level.INFO, "Weather report sent successfully");
    }
  }



  /*
  private class ShutDownTask ensures a proper logout and closing of connection after program exit
  */
  private static class ShutDownTask extends Thread {

    @Override
    public void run() {
      System.out.println("Performing shutdown");
      //only logout if login was successful
      if (successfulLogin) {
        try {
          Boolean successfulLogout = weatherClient.logout(userId);
          if (successfulLogout) {
            log.log(Level.INFO, "Logout successful");
          }
        } catch (TException e) {
          e.printStackTrace();
          log.log(Level.WARNING, "Error at login/logout");
          System.exit(2);
        }
        transport.close();
      }
    }
  }

}

