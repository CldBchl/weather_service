package weatherservice;

import org.apache.thrift.TException;
import weatherservice.thrift.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class WeatherServiceImpl implements Weather.Iface {

    private String serverName;
    private HashMap<Long,SystemWarning> systemWarnings = new HashMap<>();
    private HashMap<Location, Long> LocationIds = new HashMap<>();
    private HashMap<Long, Location> idLocations = new HashMap<>();
    private HashSet<Long> activeUsers = new HashSet<>() {};

    public WeatherServiceImpl(String name ){
        this.serverName = name;
    }

    private long generateUserId() {
        // TODO: use secure sessionID Generator;
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
    public long login(Location location) throws LocationException, TException {
        long userId;

        if (!validateLocation(location))
            throw new LocationException(location, "location has unset field");

        // generate UserDd for location if doesnt exist
        if (!LocationIds.containsKey(location)) {
            userId = generateUserId();
            LocationIds.put(location, userId);
            idLocations.put(userId,location);
        }

        // login location with its UserId if not already logged in
        if (!activeUsers.contains(LocationIds.get(location))) {
            activeUsers.add(LocationIds.get(location));
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

        WeatherReport report = new WeatherReport(
                Report.RAINY,
                idLocations.get(userId),
                16.5,
                Byte.parseByte("5"),
                Byte.parseByte("20"),
                10,
                Short.parseShort("30"),
                Short.parseShort("30"),
                time
        );

        return report;
    }

    @Override
    public WeatherWarning checkWeatherWarnings(long userId) throws UnknownUserException, TException {
        if (!validateSession(userId)){
            throw new UnknownUserException(userId, "unknown user");
        }

        return WeatherWarning.NONE;
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
