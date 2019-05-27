package weatherservice;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.input.ReversedLinesFileReader;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TMultiplexedProtocol;
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

  private static final Logger log = Logger.getLogger(WeatherServiceImpl.class.getName());
  private String serverName;
  private HashMap<Long, SystemWarning> systemWarnings = new HashMap<>();
  private HashMap<Location, Long> LocationIds = new HashMap<>();
  private HashMap<Long, Location> idLocations = new HashMap<>();
  private HashSet<Long> activeUsers = new HashSet<>() {
  };

  //synchronisation thread handlers
  private static ExecutorService syncReportExecutor;
  private static ExecutorService syncLoginExecutor;

  //synchronisation queues
  private Queue<Map.Entry<Long, WeatherReport>> syncReportUserIdQueue = new LinkedList<Map.Entry<Long, WeatherReport>>();
  private Queue<Map.Entry<Long, Location>> syncLoginQueue = new LinkedList<Map.Entry<Long, Location>>();

  //synchronisation runnables
  private SyncReportsRunnable syncReportsRunnable;
  private SyncLoginRunnable syncLoginsRunnable;

  private HashMap<WeatherSync.Client, TTransport> clientTransport = new HashMap<>();

  public WeatherServiceImpl(String name, String syncServerIp,
      int syncServerPort1, int syncServerPort2) {

    this.serverName = name;

    //initialize weather.sync clients and transport connections
    TTransport transportClient1 = new TSocket(syncServerIp, syncServerPort1);
    TBinaryProtocol protocol1 = new TBinaryProtocol(transportClient1);
    TMultiplexedProtocol mp1 = new TMultiplexedProtocol(protocol1, "WeatherSync");
    WeatherSync.Client syncClient1 = new WeatherSync.Client(mp1);

    TTransport transportClient2 = new TSocket(syncServerIp, syncServerPort2);
    TBinaryProtocol protocol2 = new TBinaryProtocol(transportClient2);
    TMultiplexedProtocol mp2 = new TMultiplexedProtocol(protocol2, "WeatherSync");
    WeatherSync.Client syncClient2 = new WeatherSync.Client(mp2);

    clientTransport.put(syncClient1, transportClient1);
    clientTransport.put(syncClient2, transportClient2);

    //the syncReportExecutor manages a single thread for synchronizing weather reports among the thrift servers
    syncReportExecutor = Executors.newSingleThreadExecutor();
    syncReportsRunnable = new SyncReportsRunnable();
    syncLoginExecutor = Executors.newSingleThreadExecutor();
    syncLoginsRunnable = new SyncLoginRunnable();
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

    // generate UserId for location if doesnt exist
    if (!LocationIds.containsKey(location)) {
      userId = generateUserId();
      LocationIds.put(location, userId);
      idLocations.put(userId, location);
    }

    //send login data to weatherAPI thrift servers
    syncLoginQueue.add(new SimpleEntry<>(Long.valueOf(userId), location));
    //performSyncLoginData(location, userId);
    syncLoginExecutor.execute(syncLoginsRunnable);

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

    //seperate thread sends weatherReport to weatherAPI servers
    syncReportUserIdQueue.add(new SimpleEntry<>(Long.valueOf(sessionToken), report));
    syncReportExecutor.execute(syncReportsRunnable);

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
    //./performSyncSystemWarning(systemWarning, userId);

    systemWarnings.putIfAbsent(userId, systemWarning);

    // validate if systemWarning got correctly saved
    return systemWarning == systemWarnings.get(userId);
  }

  @Override
  synchronized public boolean syncLoginData(weatherservice.weatherSync.Location location,
      long userId)
      throws LoggedInUserException, TException {

    //parse syncLocation to weatherLocation
    Location weatherLoacation = parseSyncLocation(location);

    if (activeUsers.contains(userId)) {
      throw new LoggedInUserException(userId, "user is already in active users list");
    } else {
      //add location and user id to lists
      if (!LocationIds.containsKey(weatherLoacation)) {
        LocationIds.put(weatherLoacation, userId);
        idLocations.put(userId, weatherLoacation);
      }
      //add location to active users
      if (!activeUsers.contains(LocationIds.get(weatherLoacation))) {
        activeUsers.add(LocationIds.get(weatherLoacation));
        System.out.println("login server " + serverName + ", active users: " + activeUsers);
        return true;
      }
      return false;
    }
  }

  @Override
  public boolean syncLogoutData(long userId)
      throws weatherservice.weatherSync.UnknownUserException, TException {

    //send logout data to weatherAPI servers
    performSyncLogoutData(userId);

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
  synchronized public boolean syncWeatherReport(weatherservice.weatherSync.WeatherReport report,
      long userId) throws weatherservice.weatherSync.UnknownUserException, TException {

    WeatherReport weatherReport = parseSyncWeatherReport(report);

    System.out
        .println("weatherreport sync server " + serverName + ", active users: " + activeUsers);

    if (!validateSession(userId)) {
      throw new UnknownUserException(userId, "unknown user");
    }

    // write report to file
    try {
      new File("./serverData/" + serverName).mkdirs();
      FileWriter file = new FileWriter("./serverData/" + serverName + "/" + userId + ".txt",
          true);
      file.append(weatherReport.toString());
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
  public boolean syncSystemWarning(weatherservice.weatherSync.SystemWarning systemWarning,
      long userId) throws weatherservice.weatherSync.UnknownUserException, TException {

    if (!validateSession(userId)) {
      throw new UnknownUserException(userId, "unknown user");
    }

    SystemWarning warning = SystemWarning.findByValue(systemWarning.getValue());

    systemWarnings.putIfAbsent(userId, warning);

    // validate if systemWarning got correctly saved
    return warning == systemWarnings.get(userId);

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
    weatherReport.humidity = report.humidity;
    weatherReport.rainfall = report.rainfall;
    weatherReport.temperature = report.temperature;
    weatherReport.windStrength = report.windStrength;

    return weatherReport;
  }

  private void performSyncLoginData(Location location, long userId) {
//send updates to every client in the clientTransport map
    for (Map.Entry<Client, TTransport> entry : clientTransport.entrySet()) {
      Client client = entry.getKey();
      TTransport transport = entry.getValue();
      try {
        if (!transport.isOpen()) {
          transport.open();
        }
        try {
          if (client.syncLoginData(parseWeatherLocation(location), userId)) {
            log.log(Level.INFO, "Login update successfully sent to server " + transport.toString());
          } else {
            log.log(Level.WARNING,
                "Error when sending login update to server " + transport.toString());
          }
        } catch (TException e) {
          e.printStackTrace();
        }
      } catch (TTransportException e) {
        log.log(Level.WARNING,
            "Connection error: Could not send login update to server " + transport.toString());
      }
    }
  }

  private void performSyncLogoutData(long userId) {
    //send updates to every client in the clientTransport map
    for (Map.Entry<Client, TTransport> entry : clientTransport.entrySet()) {
      Client client = entry.getKey();
      TTransport transport = entry.getValue();
      try {
        if (!transport.isOpen()) {
          transport.open();
        }
        try {
          if (client.syncLogoutData(userId)) {
            log.log(Level.INFO,
                "Logout update successfully sent to server " + transport.toString());
          } else {
            log.log(Level.WARNING,
                "Error when sending logout update to server " + transport.toString());
          }
        } catch (TException e) {
          e.printStackTrace();
        }
        transport.close();
      } catch (TTransportException e) {
        log.log(Level.WARNING,
            "Connection error: Could not send logout update to server" + transport.toString());
      }
    }

  }

  private void performSyncWeatherReport(WeatherReport weatherReport, long userId) {
//send updates to every client in the clientTransport map
    for (Map.Entry<Client, TTransport> entry : clientTransport.entrySet()) {
      Client client = entry.getKey();
      TTransport transport = entry.getValue();
      try {
        if (!transport.isOpen()) {
          transport.open();
        }
        try {
          if (client.syncWeatherReport(parseWeatherReport(weatherReport), userId)) {
            log.log(Level.INFO,
                "Weather report update successfully sent to server " + transport.toString());
          } else {
            log.log(Level.WARNING,
                "Error when sending weather report update to server " + transport.toString());
          }
        } catch (TException e) {
          e.printStackTrace();
        }
      } catch (TTransportException e) {
        log.log(Level.WARNING,
            "Connection error: Could not send weatherReport update to server " + transport
                .toString());
      }
    }
  }

  private void performSyncSystemWarning(SystemWarning systemWarning, long userId) {
//send updates to every client in the clientTransport map
    for (Map.Entry<Client, TTransport> entry : clientTransport.entrySet()) {
      Client client = entry.getKey();
      TTransport transport = entry.getValue();
      try {
        if (!transport.isOpen()) {
          transport.open();
        }
        try {
          weatherservice.weatherSync.SystemWarning warning = weatherservice.weatherSync.SystemWarning
              .findByValue(systemWarning.getValue());
          if (client.syncSystemWarning(warning, userId)) {
            log.log(Level.INFO,
                "System warning update successful sent to server " + transport.toString());
          } else {
            log.log(Level.WARNING,
                "Error when sending system warning update to server " + transport.toString());
          }
        } catch (TException e) {
          e.printStackTrace();
        }
      } catch (TTransportException e) {
        log.log(Level.WARNING,
            "Connection error: Could not send systemWarning update to server " + transport
                .toString());
      }
    }
  }

  public class SyncReportsRunnable implements Runnable {

    public SyncReportsRunnable() {
    }

    public void run() {
      System.out.println("Start syncRunnable thread " + this);
      Map.Entry<Long, WeatherReport> entry = syncReportUserIdQueue.poll();
      if (!entry.equals(null)) {
        WeatherReport report = entry.getValue();
        long userId = entry.getKey();
        performSyncWeatherReport(report, userId);
        System.out.println("close syncRunnable thread " + this);
      }
    }

  }


  public class SyncLoginRunnable implements Runnable {

    public SyncLoginRunnable() {
    }

    public void run() {
      System.out.println("Start syncLoginRunnable thread " + this);
      Map.Entry<Long, Location> entry = syncLoginQueue.poll();
      if (!entry.equals(null)) {
        Location location = entry.getValue();
        long userId = entry.getKey();
        performSyncLoginData(location, userId);
        System.out.println("close syncLoginRunnable thread " + this);
      }
    }

  }

}