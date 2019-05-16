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
  private THttpClient transport;
  private Weather.Client weatherClient;
  private byte locationId;
  private Location location;
  private long userId;
  private Boolean successfulLogin = false;
  private WeatherReport weatherReport;

  public WStationThriftClient(String serverIP, String serverPort, String stationName, String lId) {

    String url = "http://" + serverIP + ":" + serverPort + "/weather";
    try {
      transport = new THttpClient(url);
    } catch (TTransportException e) {
      e.printStackTrace();
    }
    TBinaryProtocol protocol = new TBinaryProtocol(transport);
    weatherClient = new Weather.Client(protocol);

    //register shutDownTask to be executed when program is terminated
    ShutDownTask shutDownTask = new ShutDownTask();
    Runtime.getRuntime().addShutdownHook(shutDownTask);

    transport.open();

    locationId = Byte.parseByte(lId);
    location = new Location(locationId, stationName, 23.24, 45.45);

    weatherReport = new WeatherReport();
    weatherReport.setLocation(location);

    performLogin(weatherClient);
  }

  @Override
  public void run() {
    performSendWeatherReport(weatherClient);
  }

  private Boolean performLogin(Weather.Client client) {

    try {
      userId = client.login(location);
      System.out.println(locationId);
      log.log(Level.INFO, "Thrift Login successful");
      successfulLogin = true;
    } catch (TException e) {
      e.printStackTrace();
      log.log(Level.WARNING, "Error at login");
      successfulLogin = false;
    }

    return successfulLogin;
  }

  private Boolean performSendWeatherReport(Weather.Client client) {

    if (successfulLogin) {
      try {
        log.log(Level.INFO, "Weather report sent successfully");
        return client.sendWeatherReport(weatherReport, userId);
      } catch (TException e) {
        e.printStackTrace();
        log.log(Level.WARNING, "Weather report could not be sent");
      }
    }
    return false;
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

  }


  /*
  private class ShutDownTask ensures a proper logout and closing of connection after program exit
  */
  private class ShutDownTask extends Thread {

    @Override
    public void run() {
      System.out.println("Performing shutdown");
      //only logout if login was successful
      if (successfulLogin) {
        try {
          Boolean successfulLogout = weatherClient.logout(userId);
          if (successfulLogout) {
            //System.out.println("logout");
          }
        } catch (TException e) {
          e.printStackTrace();
          //System.out.println("Error at logout");
          System.exit(2);
        }
        transport.close();
      }
    }
  }

}

