package weatherservice;

import org.apache.thrift.TException;
import org.apache.thrift.async.AsyncMethodCallback;
import weatherservice.thrift.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

public class WeatherServiceImpl implements Weather.Iface {

    private String serverName;
    private HashMap<Long,SystemWarning> systemWarnings = new HashMap<>();
    private HashMap<Long,Location> activeUsers = new HashMap<>();
    // TODO seperate quew for acutal active users and one map to link user to location

    public WeatherServiceImpl(String name ){
        this.serverName = name;
    }

    private long generateUserId() {
        // TODO: use secure sessionID Generator;
        long id = 0;
        while (activeUsers.containsKey(id)){
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
        return activeUsers.containsKey(sessionToken);
    }



    @Override
    public long login(Location location) throws LocationException, TException {
        if (!activeUsers.containsValue(location)&& validateLocation(location)){
            long userId = generateUserId();
            activeUsers.put(userId, location);
            return userId;
        } else {
            throw new LocationException(location, "location already exists or has unset field");
        }
    }

    @Override
    public boolean logout(long sessionToken) throws UnknownUserException, TException {
        // TODO: logout user at 2 o Clock
        if(activeUsers.containsKey(sessionToken)){
            Location loc = activeUsers.get(sessionToken);
            if (activeUsers.remove(sessionToken,loc)) {
                return true;
            } else {
                return false;
            }

        } else {
            throw new UnknownUserException(sessionToken, "unknown user");
        }
    }

    @Override
    public boolean sendWeatherReport(WeatherReport report, long sessionToken) throws UnknownUserException, ReportException, DateException, LocationException, TException {
        if (!validateSession(sessionToken)){
            throw new UnknownUserException(sessionToken, "unknown user");
        }

        //new File("./sensorData/"+stationName).mkdirs();
        //FileWriter file = new FileWriter("./sensorData/" + stationName + "/" + jsonObject.get("type")+ ".txt",true);
        try {
            new File("./serverData/"+ serverName).mkdirs();
            FileWriter file = new FileWriter("./serverData/" + serverName + "/" + ".txt",true);
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
        return new WeatherReport();
    }

    @Override
    public WeatherWarning checkWeatherWarnings(long userId) throws UnknownUserException, TException {
        if (!validateSession(userId)){
            throw new UnknownUserException(userId, "unknown user");
        }
        return WeatherWarning.BLIZZARD;
    }

    @Override
    public boolean sendWarning(SystemWarning systemWarning, long userId) throws UnknownUserException, TException {
        if (!validateSession(userId)){
            throw new UnknownUserException(userId, "unknown user");
        }

        systemWarnings.putIfAbsent(userId,systemWarning);

        // validate if systemWarning got correctly registered
        if (systemWarning == systemWarnings.get(userId)){
            return true;
        } else {
            return false;
        }
    }
}
