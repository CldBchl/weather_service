package weatherservice;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import org.apache.commons.io.input.ReversedLinesFileReader;
import org.apache.thrift.TException;
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

public class WeatherServiceImpl implements Weather.Iface {

    private String serverName;
    private HashMap<Long,SystemWarning> systemWarnings = new HashMap<>();
    private HashMap<Location, Long> LocationIds = new HashMap<>();
    private HashMap<Long, Location> idLocations = new HashMap<>();
    private HashSet<Long> activeUsers = new HashSet<>() {};

    public WeatherServiceImpl(String name ){
        this.serverName = name;
        System.out.println(activeUsers);
    }

    private long generateUserId() {
        long id = 0;
        while (LocationIds.containsValue(id)){
            id++;
        }
        return id;
    }

    private boolean validateLocation(Location location) {
        // check if every value is set
        return  location.isSetLatitude() && location.isSetLocationID()
                 && location.isSetLongitude() && location.isSetName();
    }

    private boolean validateSession(long sessionToken) {
        return activeUsers.contains(sessionToken);
    }

    @Override
    synchronized public long login(Location location) throws LocationException, TException {
        long userId;
        System.out.println(location);

        if (!validateLocation(location))
            throw new LocationException(location, "location has unset field");

        // generate UserDd for location if doesnt exist
        if (!LocationIds.containsKey(location)) {
            userId = generateUserId();
            System.out.println(userId);
            LocationIds.put(location, userId);
            idLocations.put(userId,location);
            System.out.println("userId: "+ LocationIds.get(location));
            System.out.println("location: " +idLocations.get(userId));
        }

        System.out.println("Before login" +activeUsers.toString());
        // login location with its UserId if not already logged in
        if (!activeUsers.contains(LocationIds.get(location))) {
            activeUsers.add(LocationIds.get(location));
            System.out.println("After login" +activeUsers.toString());
            return LocationIds.get(location); // return userId

        } else {
            throw new LocationException(location, "location already exists or has unset field");
        }
    }

    @Override
    public boolean logout(long sessionToken) throws UnknownUserException, TException {
        // TODO: logout user at 2 o Clock?
        if(activeUsers.contains(sessionToken)){
            if (activeUsers.remove(sessionToken)) {
                systemWarnings.remove(sessionToken);
                System.out.println("successful logout");
                System.out.println("after logout" +activeUsers.toString());

                return true;
            } else {
                return false;
            }

        } else {
            throw new UnknownUserException(sessionToken, "user isn't logged in");
        }
    }


    @Override
    public boolean sendWeatherReport(WeatherReport report, long sessionToken) throws UnknownUserException, ReportException, DateException, LocationException, TException {
        if (!validateSession(sessionToken)){
            throw new UnknownUserException(sessionToken, "unknown user");
        }

        // write report to file
        try {
            new File("./serverData/"+ serverName).mkdirs();
            FileWriter file = new FileWriter("./serverData/" + serverName + "/" + sessionToken + ".txt",true);
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
    public WeatherReport receiveForecastFor(long userId, String time) throws UnknownUserException, DateException, TException {
        if (!validateSession(userId)){
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
            }
            else {
                forecast= new WeatherReport();
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

        if (!param.equals("null"))
            report.setReport(Report.findByValue(Integer.parseInt(param)));

        // get temperature
        index = string.indexOf("temperature:");
        string = string.substring(index + "temperature:".length());
        endOfParamIndex = string.indexOf(",");
        param = string.substring(0, endOfParamIndex);
        if (!param.equals("null"))
            report.setTemperature(Double.parseDouble(param));

        // get humidity
        index = string.indexOf("humidity:");
        string = string.substring(index + "humidity:".length());
        endOfParamIndex = string.indexOf(",");
        param = string.substring(0, endOfParamIndex);
        if (!param.equals("null"))
            report.setHumidity(Byte.parseByte(param));

        // get windStrength
        index = string.indexOf("windStrength:");
        string = string.substring(index + "windStrength:".length());
        endOfParamIndex = string.indexOf(",");
        param = string.substring(0, endOfParamIndex);
        if (!param.equals("null"))
            report.setWindStrength(Byte.parseByte(param));

        // get rainfall
        index = string.indexOf("rainfall:");
        string = string.substring(index + "rainfall:".length());
        endOfParamIndex = string.indexOf(",");
        param = string.substring(0, endOfParamIndex);
        if (!param.equals("null"))
            report.setRainfall(Double.parseDouble(param));

        // get atmosphericpressure
        index = string.indexOf("atmosphericpressure:");
        string = string.substring(index + "atmosphericpressure:".length());
        endOfParamIndex = string.indexOf(",");
        param = string.substring(0, endOfParamIndex);
        if (!param.equals("null"))
            report.setAtmosphericpressure(Short.parseShort(param));

        // get windDirection
        index = string.indexOf("windDirection:");
        string = string.substring(index + "windDirection:".length());
        endOfParamIndex = string.indexOf(",");
        param = string.substring(0, endOfParamIndex);
        report.setWindDirection(Short.parseShort(param));

        // get dateTime
        index = string.indexOf("dateTime:");
        string = string.substring(index + "dateTime:".length() -1);
        endOfParamIndex = string.indexOf(")");
        param = string.substring(0, endOfParamIndex);
        if (!param.equals("null"))
            report.setDateTime(param);

        return report;
    }


    //WeatherReport(report:null, location:Location(locationID:1, name:Station2, latitude:23.24, longitude:45.45),
    // temperature:12.34, humidity:12, windStrength:0, rainfall:2.63, atmosphericpressure:0, windDirection:0, dateTime:2019-05-18T09:31:46+02:00)

    @Override
    public WeatherWarning checkWeatherWarnings(long userId) throws UnknownUserException, TException {
        if (!validateSession(userId)){
            throw new UnknownUserException(userId, "unknown user");
        }

        WeatherReport report = readReport(userId);
        if (report.getRainfall() > 100 && report.getWindStrength() > 50 && report.getTemperature() < -2){
            return WeatherWarning.BLIZZARD;
        } else if (report.getRainfall() > 500){
            return WeatherWarning.FLOOD;
        } else if (report.getWindStrength() > 120 ){
            return (WeatherWarning.HURRICANE);
        } if (report.getRainfall() > 50 && report.getWindStrength() < 30){
            return WeatherWarning.STORM;
        } else if (report.getWindStrength() > 60){
            return WeatherWarning.TORNADO;
        } else if (report.getTemperature() > 50){
            return WeatherWarning.UV;
        } else {
            return WeatherWarning.NONE;
        }
    }

    @Override
    public boolean sendWarning(SystemWarning systemWarning, long userId) throws UnknownUserException, TException {
        if (!validateSession(userId)){
            throw new UnknownUserException(userId, "unknown user");
        }

        systemWarnings.putIfAbsent(userId,systemWarning);

        // validate if systemWarning got correctly saved
        return systemWarning == systemWarnings.get(userId);
    }
}
