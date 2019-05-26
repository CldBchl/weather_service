package weatherservice;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import org.apache.commons.io.input.ReversedLinesFileReader;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import weatherservice.thrift.DateException;
import weatherservice.thrift.Location;
import weatherservice.thrift.LocationException;
import weatherservice.thrift.Report;
import weatherservice.thrift.ReportException;
import weatherservice.thrift.SystemWarning;
import weatherservice.thrift.UnknownUserException;
import weatherservice.thrift.Weather;
import weatherservice.thrift.WeatherReport;
import weatherservice.thrift.WeatherWarning;
import weatherservice.weatherSync.LoggedInUserException;
import weatherservice.weatherSync.WeatherSync;
import weatherservice.weatherSync.WeatherSync.Client;

public class WeatherServiceImpl implements Weather.Iface, WeatherSync.Iface {

  private String serverName;
  private HashMap<Long, SystemWarning> systemWarnings = new HashMap<>();
  private HashMap<Location, Long> LocationIds = new HashMap<>();
  private HashMap<Long, Location> idLocations = new HashMap<>();
  private HashSet<Long> activeUsers = new HashSet<>() {
  };

  private HashMap<WeatherSync.Client, TTransport> clientTransport = new HashMap<>();

  public WeatherServiceImpl(String name, String syncServerIp,
      int syncServerPort1, int syncServerPort2) {

    this.serverName = name;

    //initialize weather.sync clients and transport connections
    TTransport transportClient1 = new TSocket(syncServerIp, syncServerPort1);
    TBinaryProtocol protocol1 = new TBinaryProtocol(transportClient1);
    WeatherSync.Client syncClient1 = new WeatherSync.Client(protocol1);

    TTransport transportClient2 = new TSocket(syncServerIp, syncServerPort2);
    TBinaryProtocol protocol2 = new TBinaryProtocol(transportClient2);
    WeatherSync.Client syncClient2 = new WeatherSync.Client(protocol2);

    clientTransport.put(syncClient1, transportClient1);
    clientTransport.put(syncClient2, transportClient2);

  }


  private long generateUserId() {
    long id = 0;
    while (LocationIds.containsValue(id)) {
      id++;
    }
    return id;
  }

  private boolean validateLocation(Location location) {
    // check if every value is set
    return location.isSetLatitude() && location.isSetLocationID()
        && location.isSetLongitude() && location.isSetName();
  }

  private boolean validateSession(long sessionToken) {
    return activeUsers.contains(sessionToken);
  }

  @Override
  synchronized public long login(Location location) throws LocationException, TException {
    long userId = 0;

    if (!validateLocation(location)) {
      throw new LocationException(location, "location has unset field");
    }

    // generate UserDd for location if doesnt exist
    if (!LocationIds.containsKey(location)) {
      userId = generateUserId();
      LocationIds.put(location, userId);
      idLocations.put(userId, location);
    }

    //send login data to weatherAPI thrift servers
    performSyncLoginData(location, userId);

    // login location with its UserId if not already logged in
    if (!activeUsers.contains(LocationIds.get(location))) {
      activeUsers.add(LocationIds.get(location));
      return LocationIds.get(location); // return userId

    } else {
      throw new LocationException(location, "location already exists or has unset field");
    }

  }

  @Override
  synchronized public boolean logout(long sessionToken) throws UnknownUserException, TException {

    //send logout data to weatherAPI thrift servers
    performSyncLogoutData(sessionToken);

    if (activeUsers.contains(sessionToken)) {
      if (activeUsers.remove(sessionToken)) {
        systemWarnings.remove(sessionToken);
        return true;
      } else {
        return false;
      }

    } else {
      throw new UnknownUserException(sessionToken, "user isn't logged in");
    }
  }


  @Override
  public boolean sendWeatherReport(WeatherReport report, long sessionToken)
      throws UnknownUserException, ReportException, DateException, LocationException, TException {
    if (!validateSession(sessionToken)) {
      throw new UnknownUserException(sessionToken, "unknown user");
    }

    //send weatherReport to weatherAPI servers
    performSyncWeatherReport(report, sessionToken);

    // write report to file
    try {
      new File("./serverData/" + serverName).mkdirs();
      FileWriter file = new FileWriter("./serverData/" + serverName + "/" + sessionToken + ".txt",
          true);
      file.append(report.toString());
      file.append("\n");
      file.flush();
      return true;
    } catch (IOException e) {
      e.printStackTrace();
      System.out.println("Could not create or write to file");
    }
    return false;
  }

  @Override
  public WeatherReport receiveForecastFor(long userId, String time)
      throws UnknownUserException, DateException, TException {
    if (!validateSession(userId)) {
      throw new UnknownUserException(userId, "unknown user");
    }

    WeatherReport forecast = readReport(userId);

    forecast.setLocation(idLocations.get(userId));
    forecast.setDateTime(time);

    return forecast;
  }

  private WeatherReport readReport(long userId) {
    StringBuilder reportsBuilder = new StringBuilder();
    WeatherReport forecast = new WeatherReport();

    try {
      File file = new File(("./serverData/" + serverName + "/" + userId + ".txt"));

      if (file.exists()) {
        ReversedLinesFileReader rf = new ReversedLinesFileReader(file, UTF_8);

        // read last Report
        reportsBuilder.append(rf.readLine());
        forecast = buildReport(reportsBuilder.toString());
      } else {
        forecast = new WeatherReport();
      }
    } catch (IOException e) {
      e.printStackTrace();
      System.out.println("Could not create or write to file");
    }

    return forecast;
  }

  private WeatherReport buildReport(String string) {
    WeatherReport report = new WeatherReport();
    int index, endOfParamIndex;
    String param;

    //get Report
    index = string.indexOf("report:");
    string = string.substring(index + "report:".length());
    endOfParamIndex = string.indexOf(",");
    param = string.substring(0, endOfParamIndex);

    if (!param.equals("null")) {
      report.setReport(Report.findByValue(Integer.parseInt(param)));
    }

    // get temperature
    index = string.indexOf("temperature:");
    string = string.substring(index + "temperature:".length());
    endOfParamIndex = string.indexOf(",");
    param = string.substring(0, endOfParamIndex);
    if (!param.equals("null")) {
      report.setTemperature(Double.parseDouble(param));
    }

    // get humidity
    index = string.indexOf("humidity:");
    string = string.substring(index + "humidity:".length());
    endOfParamIndex = string.indexOf(",");
    param = string.substring(0, endOfParamIndex);
    if (!param.equals("null")) {
      report.setHumidity(Byte.parseByte(param));
    }

    // get windStrength
    index = string.indexOf("windStrength:");
    string = string.substring(index + "windStrength:".length());
    endOfParamIndex = string.indexOf(",");
    param = string.substring(0, endOfParamIndex);
    if (!param.equals("null")) {
      report.setWindStrength(Byte.parseByte(param));
    }

    // get rainfall
    index = string.indexOf("rainfall:");
    string = string.substring(index + "rainfall:".length());
    endOfParamIndex = string.indexOf(",");
    param = string.substring(0, endOfParamIndex);
    if (!param.equals("null")) {
      report.setRainfall(Double.parseDouble(param));
    }

    // get atmosphericpressure
    index = string.indexOf("atmosphericpressure:");
    string = string.substring(index + "atmosphericpressure:".length());
    endOfParamIndex = string.indexOf(",");
    param = string.substring(0, endOfParamIndex);
    if (!param.equals("null")) {
      report.setAtmosphericpressure(Short.parseShort(param));
    }

    // get windDirection
    index = string.indexOf("windDirection:");
    string = string.substring(index + "windDirection:".length());
    endOfParamIndex = string.indexOf(",");
    param = string.substring(0, endOfParamIndex);
    report.setWindDirection(Short.parseShort(param));

    // get dateTime
    index = string.indexOf("dateTime:");
    string = string.substring(index + "dateTime:".length() - 1);
    endOfParamIndex = string.indexOf(")");
    param = string.substring(0, endOfParamIndex);
    if (!param.equals("null")) {
      report.setDateTime(param);
    }

    return report;
  }

  //WeatherReport(report:null, location:Location(locationID:1, name:Station2, latitude:23.24, longitude:45.45),
  // temperature:12.34, humidity:12, windStrength:0, rainfall:2.63, atmosphericpressure:0, windDirection:0, dateTime:2019-05-18T09:31:46+02:00)

  @Override
  public WeatherWarning checkWeatherWarnings(long userId) throws UnknownUserException, TException {
    if (!validateSession(userId)) {
      throw new UnknownUserException(userId, "unknown user");
    }

    WeatherReport report = readReport(userId);
    if (report.getRainfall() > 100 && report.getWindStrength() > 50
        && report.getTemperature() < -2) {
      return WeatherWarning.BLIZZARD;
    } else if (report.getRainfall() > 500) {
      return WeatherWarning.FLOOD;
    } else if (report.getWindStrength() > 120) {
      return (WeatherWarning.HURRICANE);
    }
    if (report.getRainfall() > 50 && report.getWindStrength() < 30) {
      return WeatherWarning.STORM;
    } else if (report.getWindStrength() > 60) {
      return WeatherWarning.TORNADO;
    } else if (report.getTemperature() > 50) {
      return WeatherWarning.UV;
    } else {
      return WeatherWarning.NONE;
    }
  }

  @Override
  public boolean sendWarning(SystemWarning systemWarning, long userId)
      throws UnknownUserException, TException {
    if (!validateSession(userId)) {
      throw new UnknownUserException(userId, "unknown user");
    }

    //send system warning update to other weatherAPI servers
    performSyncSystemWarning(systemWarning, userId);

    systemWarnings.putIfAbsent(userId, systemWarning);

    // validate if systemWarning got correctly saved
    return systemWarning == systemWarnings.get(userId);
  }

  @Override
  public boolean syncLoginData(weatherservice.weatherSync.Location location, long userId)
      throws LoggedInUserException, TException {
    if (activeUsers.contains(userId)) {
      throw new LoggedInUserException(userId, "user is already in active users list");
    } else {
      //add location and user id to lists
      if (!LocationIds.containsKey(parseSyncLocation(location))) {
        LocationIds.put(parseSyncLocation(location), userId);
        idLocations.put(userId, parseSyncLocation(location));
      }
      //add location to active users
      if (!activeUsers.contains(LocationIds.get(location))) {
        activeUsers.add(LocationIds.get(location));
        return true;
      }
      return false;
    }
  }

  @Override
  public boolean syncLogoutData(long userId)
      throws weatherservice.weatherSync.UnknownUserException, TException {
    if (activeUsers.contains(userId)) {
      if (activeUsers.remove(userId)) {
        systemWarnings.remove(userId);
        return true;
      } else {
        return false;
      }

    } else {
      throw new UnknownUserException(userId, "user was not found in active users list");
    }
  }

  @Override
  public boolean syncWeatherReport(weatherservice.weatherSync.WeatherReport weatherReport,
      long userId) throws weatherservice.weatherSync.UnknownUserException, TException {

    return sendWeatherReport(parseSyncWeatherReport(weatherReport), userId);
  }

  @Override
  public boolean syncSystemWarning(weatherservice.weatherSync.SystemWarning systemWarning,
      long userId) throws weatherservice.weatherSync.UnknownUserException, TException {

    SystemWarning warning = SystemWarning.findByValue(systemWarning.getValue());

    return sendWarning(warning, userId);
  }

  private weatherservice.thrift.Location parseSyncLocation(
      weatherservice.weatherSync.Location location) {
    weatherservice.thrift.Location weatherLocation = new weatherservice.thrift.Location();
    weatherLocation.name = location.name;
    weatherLocation.description = location.description;
    weatherLocation.latitude = location.latitude;
    weatherLocation.longitude = location.longitude;
    weatherLocation.locationID = location.locationID;

    return weatherLocation;
  }


  private weatherservice.thrift.WeatherReport parseSyncWeatherReport(
      weatherservice.weatherSync.WeatherReport report) {
    weatherservice.thrift.WeatherReport weatherReport = new weatherservice.thrift.WeatherReport();
    weatherReport.location = parseSyncLocation(report.location);
    weatherReport.dateTime = report.dateTime;
    weatherReport.report = Report.findByValue(report.report.getValue());
    weatherReport.humidity = report.humidity;
    weatherReport.rainfall = report.rainfall;
    weatherReport.temperature = report.temperature;
    weatherReport.windStrength = report.windStrength;

    return weatherReport;
  }

  private weatherservice.weatherSync.Location parseWeatherLocation(
      weatherservice.thrift.Location weatherLocation) {
    weatherservice.weatherSync.Location location = new weatherservice.weatherSync.Location();
    location.name = weatherLocation.name;
    location.description = weatherLocation.description;
    location.latitude = weatherLocation.latitude;
    location.longitude = weatherLocation.longitude;
    location.locationID = weatherLocation.locationID;
    return location;
  }

  private weatherservice.weatherSync.WeatherReport parseWeatherReport(
      weatherservice.thrift.WeatherReport report) {
    weatherservice.weatherSync.WeatherReport weatherReport = new weatherservice.weatherSync.WeatherReport();
    weatherReport.location = parseWeatherLocation(report.location);
    weatherReport.dateTime = report.dateTime;
    weatherReport.report = weatherservice.weatherSync.Report.findByValue(report.report.getValue());
    weatherReport.humidity = report.humidity;
    weatherReport.rainfall = report.rainfall;
    weatherReport.temperature = report.temperature;
    weatherReport.windStrength = report.windStrength;

    return weatherReport;
  }

  private void performSyncLoginData(Location location, long userId) {

    for (Map.Entry<Client, TTransport> entry : clientTransport.entrySet()) {
      Client client = entry.getKey();
      TTransport transport = entry.getValue();
      try {
        transport.open();

        try {
          if (client.syncLoginData(parseWeatherLocation(location), userId)) {
            System.out
                .println("Login update successfully sent to server " + transport.toString());
          }
        } catch (TException e) {
          e.printStackTrace();
        }
        transport.close();
      } catch (TTransportException e) {
        System.out
            .println("Could not send login update to server " + transport.toString());
      }
    }
  }

  private void performSyncLogoutData(long userId) {
    for (Map.Entry<Client, TTransport> entry : clientTransport.entrySet()) {
      Client client = entry.getKey();
      TTransport transport = entry.getValue();
      try {
        transport.open();

        try {
          if (client.syncLogoutData(userId)) {
            System.out
                .println("Logout update successfully sent to server " + transport.toString());
          }
        } catch (TException e) {
          e.printStackTrace();
        }
        transport.close();
      } catch (TTransportException e) {
        System.out
            .println("Could not send logout update to server" + transport.toString());
      }
    }

  }

  private void performSyncWeatherReport(WeatherReport weatherReport, long userId) {

    for (Map.Entry<Client, TTransport> entry : clientTransport.entrySet()) {
      Client client = entry.getKey();
      TTransport transport = entry.getValue();
      try {
        transport.open();

        try {
          if (client.syncWeatherReport(parseWeatherReport(weatherReport), userId)) {
            System.out
                .println(
                    "Weather report update successfully sent to server " + transport.toString());
          }
        } catch (TException e) {
          e.printStackTrace();
        }
        transport.close();
      } catch (TTransportException e) {
        System.out.println(
            "Could not send weatherReport update to server " + transport.toString());
      }
    }
  }

  private void performSyncSystemWarning(SystemWarning systemWarning, long userId) {

    for (Map.Entry<Client, TTransport> entry : clientTransport.entrySet()) {
      Client client = entry.getKey();
      TTransport transport = entry.getValue();
      try {
        transport.open();

        try {
          weatherservice.weatherSync.SystemWarning warning = weatherservice.weatherSync.SystemWarning
              .findByValue(systemWarning.getValue());
          if (client.syncSystemWarning(warning, userId)) {
            System.out
                .println("System warning update successful sent to server " + transport.toString());
          }
        } catch (TException e) {
          e.printStackTrace();
        }
        transport.close();
      } catch (TTransportException e) {
        System.out.println(
            "Could not send systemWarning update to server " + transport.toString());
      }
    }
  }


}