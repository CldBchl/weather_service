package code.weatherstation;


import code.weatherstation.thrift.Location;
import code.weatherstation.thrift.Weather;
import code.weatherstation.thrift.Weather.Client;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TJSONProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;


public class WStationThriftClient {
  private static final Logger log = Logger.getLogger( WStationThriftClient.class.getName());
  //TFramedTransport is needed with non-blocking servers
  private TFramedTransport tFramedTransport;
  private String stationName;
  private TJSONProtocol protocol;
  private Weather.Client weatherClient;
  private static byte locationID=0; //manage locationIDs
  private Location location;
  private long userId;

  public WStationThriftClient(String serverIP, String serverPort, String sName){

    log.log(Level.INFO, "enter thrift client");
    stationName=sName;
    TTransport transport=new TSocket(serverIP,Integer.parseInt(serverPort));
    tFramedTransport= new TFramedTransport(transport);
    protocol= new TJSONProtocol(tFramedTransport);
    weatherClient= new Client(protocol);
    log.log(Level.INFO, "going on in  thrift client");

    try {
      transport.open();
    } catch (TTransportException e) {
      e.printStackTrace();
      log.log(Level.WARNING, "Could not establish connection to Thrift server");
    }

    location= new Location(locationID,stationName, 23.23, 45.45);


    try {
      userId= weatherClient.login(location);
      log.log(Level.INFO, "Login successful");
      locationID++; //increase id for the next object
    } catch (TException e) {
      e.printStackTrace();
      log.log(Level.WARNING, "Thrift-Login failed");
    }

    try {
      Boolean successfulLogout=weatherClient.logout(userId);
      if (successfulLogout)
        log.log(Level.INFO, "Login successful");
    } catch (TException e) {
      e.printStackTrace();
      log.log(Level.WARNING, "Thrift-Logout failed");
    }

    transport.close();
  }


}

