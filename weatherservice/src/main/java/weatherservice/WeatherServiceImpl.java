package weatherservice;

import org.apache.thrift.TException;
import org.apache.thrift.async.AsyncMethodCallback;
import weatherservice.thrift.*;

import java.util.HashMap;

public class WeatherServiceImpl implements Weather.AsyncIface {

    private HashMap<Long,Location> activeUsers = new HashMap<>();

    @Override
    public void login(Location location, AsyncMethodCallback<Long> resultHandler) throws TException {
        if (!activeUsers.containsKey(location)&& isValid(location)){
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

    private boolean isValid(Location location) {
        // check if every value is set
        return location.isSetDescription() && location.isSetLatitude() && location.isSetLocationID()
                && location.isSetDescription() && location.isSetLongitude() && location.isSetName();
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

    }

    @Override
    public void receiveForecastFor(long userId, String time, AsyncMethodCallback<WeatherReport> resultHandler) throws TException {

    }

    @Override
    public void checkWeatherWarnings(long userId, AsyncMethodCallback<WeatherWarning> resultHandler) throws TException {

    }

    @Override
    public void sendWarning(SystemWarning systemWarning, long userId, AsyncMethodCallback<Boolean> resultHandler) throws TException {

    }


}
