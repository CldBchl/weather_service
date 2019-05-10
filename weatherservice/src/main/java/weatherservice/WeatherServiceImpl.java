package weatherservice;

import org.apache.thrift.TException;
import org.apache.thrift.async.AsyncMethodCallback;
import weatherservice.thrift.*;

public class WeatherServiceImpl implements Weather.AsyncIface {
    @Override
    public void login(Location location, AsyncMethodCallback<Long> resultHandler) throws TException {

    }

    @Override
    public void logout(long sessionToken, AsyncMethodCallback<Boolean> resultHandler) throws TException {

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
