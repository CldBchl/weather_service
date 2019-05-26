package code.weatherstation;

import code.weatherstation.thrift.Location;
import code.weatherstation.thrift.SystemWarning;
import code.weatherstation.thrift.Weather;
import code.weatherstation.thrift.WeatherReport;
import code.weatherstation.thrift.WeatherWarning;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;


public class WStationThriftClient implements Runnable {

  private static final Logger log = Logger.getLogger(WStationThriftClient.class.getName());

  private TTransport transport;
  private Weather.Client weatherClient;
  private byte locationId;
  private Location location;
  private long userId;
  private Boolean successfulLogin = false;
  private WeatherReport weatherReport;

  public WStationThriftClient(String serverIP, String serverPort, String stationName, String lId) {

    transport = new TSocket(serverIP, Integer.parseInt(serverPort));

    TBinaryProtocol protocol = new TBinaryProtocol(transport);
    weatherClient = new Weather.Client(protocol);

    //register shutDownTask to be executed when program is terminated
    ShutDownTask shutDownTask = new ShutDownTask();
    Runtime.getRuntime().addShutdownHook(shutDownTask);

    try {
      transport.open();
    } catch (TTransportException e) {
      log.log(Level.WARNING, "Error when opening connection");
      e.printStackTrace();
    }

    locationId = Byte.parseByte(lId);
    location = new Location(locationId, stationName, 23.24, 45.45);

    weatherReport = new WeatherReport();
    weatherReport.setLocation(location);

    performLogin(weatherClient);
    performReceiveWeatherForecast(weatherClient);
    performCheckWeatherwarning(weatherClient);
    performSendSystemWarning(weatherClient);
  }

  @Override
  public void run() {
    performSendWeatherReport(weatherClient);
  }

  private void performLogin(Weather.Client client) {

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

  }

  private void performSendWeatherReport(Weather.Client client) {

    if (successfulLogin) {
      try {
        if (client.sendWeatherReport(weatherReport, userId)) {
          log.log(Level.INFO, "Weather report sent successfully");
        } else {
          log.log(Level.WARNING, "Weather report could not be sent");
        }

      } catch (TException e) {
        e.printStackTrace();
        log.log(Level.WARNING, "Weather report could not be sent");
      }
    }

  }


  public void updateWeatherReport(WeatherReport update) {

    //update report with current sensor values
    weatherReport.setTemperature(update.getTemperature());
    weatherReport.setRainfall(update.getRainfall());
    weatherReport.setHumidity(update.getHumidity());
    weatherReport.setWindStrength(update.getWindStrength());

    //set current time
    String currentTimeISO8601 = ZonedDateTime.now(ZoneId.of("Europe/Paris"))
        .truncatedTo(ChronoUnit.SECONDS)
        .format(DateTimeFormatter.ISO_DATE_TIME);
    currentTimeISO8601 = currentTimeISO8601.replace("[Europe/Paris]", "");
    weatherReport.setDateTime((currentTimeISO8601));

  }

  private void performReceiveWeatherForecast(Weather.Client client) {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH);
    String currentTimeISO8601Short = ZonedDateTime.now().format(formatter);
    System.out.println(currentTimeISO8601Short);
    try {
      WeatherReport forecast = client.receiveForecastFor(userId, currentTimeISO8601Short);
      log.log(Level.INFO, "Forecast was received successfully");
      System.out.println("The current forecast for location " + locationId + " is: " + forecast);
    } catch (TException e) {
      e.printStackTrace();
      log.log(Level.WARNING, "Forecast could not be received");
    }
  }


  private void performCheckWeatherwarning(Weather.Client client) {
    try {
      WeatherWarning weatherWarning = client.checkWeatherWarnings(userId);
      log.log(Level.INFO, "Weatherwarning was received successfully");
      System.out.println(
          "The current weatherwarning for location " + locationId + " is: " + weatherWarning);
    } catch (TException e) {
      e.printStackTrace();
    }
  }

  private void performSendSystemWarning(Weather.Client client) {
    Random randomGenerator = new Random();
    int warningValue = randomGenerator.nextInt(4) + 1;
    SystemWarning warning = SystemWarning.findByValue(warningValue);
    System.out.println("The current system warning for location " + locationId + " is: " + warning);

    try {
      if (client.sendWarning(warning, userId)) {
        log.log(Level.INFO, "Systemwarning was received successfully");
      } else {
        log.log(Level.INFO, "Systemwarning was not received");
      }
    } catch (TException e) {
      log.log(Level.WARNING, "Systemwarning could not be sent");
      e.printStackTrace();
    }

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
          boolean successfulLogout = weatherClient.logout(userId);
          if (successfulLogout) {
            //System.out.println("logout");
          }
        } catch (TException e) {
          //e.printStackTrace();
          System.out.println("Server already down");
          System.exit(2);
        }
        transport.close();
      }
    }
  }

}

