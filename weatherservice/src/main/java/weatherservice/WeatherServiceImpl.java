package weatherservice;

import org.apache.thrift.TException;
import org.apache.thrift.async.AsyncMethodCallback;
import weatherservice.thrift.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

public class WeatherServiceImpl implements Weather.AsyncIface {

    private String serverName;
    private HashMap<Long,SystemWarning> systemWarnings = new HashMap<>();
    private HashMap<Long,Location> activeUsers = new HashMap<>();
    // TODO seperate quew for acutal active users and one map to link user to location

    public WeatherServiceImpl(String name ){
        this.serverName = name;
    }

    @Override
    public void login(Location location, AsyncMethodCallback<Long> resultHandler) throws TException {
        if (!activeUsers.containsKey(location)&& validateLocation(location)){
            long userId = generateUserId();
            activeUsers.put(userId, location);
            resultHandler.onComplete(userId);
        } else {
            resultHandler.onError(new LocationException(location, "location already exists or has unset field"));
            throw new LocationException(location, "location already exists or has unset field");
        }
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
        return location.isSetDescription() && location.isSetLatitude() && location.isSetLocationID()
                && location.isSetDescription() && location.isSetLongitude() && location.isSetName();
    }

    private boolean validateSession(long sessionToken) {
        return activeUsers.containsKey(sessionToken);
    }


    @Override
    public void logout(long sessionToken, AsyncMethodCallback<Boolean> resultHandler) throws TException {
        // TODO: logout user at 2 o Clock
        if(activeUsers.containsKey(sessionToken)){
            Location loc = activeUsers.get(sessionToken);
            if (activeUsers.remove(sessionToken,loc)) {
                resultHandler.onComplete(true);
            } else {
                resultHandler.onComplete(false);
            }

        } else {
            resultHandler.onError(new UnknownUserException(sessionToken, "unknown user"));
            throw new UnknownUserException(sessionToken, "unknown user");
        }
    }

    @Override
    public void sendWeatherReport(WeatherReport report, long sessionToken, AsyncMethodCallback<Boolean> resultHandler) throws TException {
        if (!validateSession(sessionToken)){
            resultHandler.onError(new UnknownUserException(sessionToken, "unknown user"));
            throw new UnknownUserException(sessionToken, "unknown user");
        }

        try {
            new File("./serverData/"+ serverName).mkdirs();
            FileWriter file = new FileWriter("./serverData/" + serverName + "/" + ".txt",true);
            file.append(report.toString());
            file.append("\n");
            file.flush();
            resultHandler.onComplete(true);
        } catch (IOException e) {
            e.printStackTrace();
            resultHandler.onError(e);
            System.out.println("Could not create or write to file");
        }
        resultHandler.onComplete(false);
    }

    @Override
    public void receiveForecastFor(long userId, String time, AsyncMethodCallback<WeatherReport> resultHandler) throws TException {
        if (!validateSession(userId)){
            resultHandler.onError(new UnknownUserException(userId, "unknown user"));
            throw new UnknownUserException(userId, "unknown user");
        }
    }

    @Override
    public void checkWeatherWarnings(long userId, AsyncMethodCallback<WeatherWarning> resultHandler) throws TException {
        if (!validateSession(userId)){
            resultHandler.onError(new UnknownUserException(userId, "unknown user"));
            throw new UnknownUserException(userId, "unknown user");
        }
    }

    @Override
    public void sendWarning(SystemWarning systemWarning, long userId, AsyncMethodCallback<Boolean> resultHandler) throws TException {
        if (!validateSession(userId)){
            resultHandler.onError(new UnknownUserException(userId, "unknown user"));
            throw new UnknownUserException(userId, "unknown user");
        }

       systemWarnings.putIfAbsent(userId,systemWarning);

        // validate if systemWarning got correctly registered
        if (systemWarning == systemWarnings.get(userId)){
            resultHandler.onComplete(true);
        } else {
            resultHandler.onComplete(false);
        }
    }
}
