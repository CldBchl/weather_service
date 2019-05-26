package weatherservice;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;
import org.apache.thrift.transport.TTransportException;
import weatherservice.thrift.Weather;

/*
 * The RpcServer class
 */
public class RpcServer implements Runnable {

  private static final Logger log = Logger.getLogger(RpcServer.class.getName());
  private int serverPort;
  private String serverName;

  //default settings for TThreadpoolserver is IP = 0.0.0.0
  private String syncServerIp = "0.0.0.0";
  private int syncServerPort1;
  private int syncServerPort2;
  private TThreadPoolServer server;

  public RpcServer(String name, int port, int syncServerPort1, int syncServerPort2) {
    serverPort = port;
    this.syncServerPort1 = syncServerPort1;
    this.syncServerPort2 = syncServerPort2;
    serverName=name;
  }

  public void start() throws TTransportException {
    TServerTransport serverTransport = new TServerSocket(serverPort);
    server = new TThreadPoolServer(new TThreadPoolServer.Args(serverTransport)
        .processor(new Weather.Processor<>(
            new WeatherServiceImpl(serverName, syncServerIp, syncServerPort1, syncServerPort2))));

    log.log(Level.INFO, "Starting server "+serverName+",port "+serverPort+", connecting to port "+syncServerPort1+" and "+syncServerPort2);

    server.serve();

    log.log(Level.INFO, "Server started");
  }

  public void stop() {
    if (server != null && server.isServing()) {

      log.log(Level.INFO, "Stopping server");

      server.stop();

      log.log(Level.INFO, "Server stopped");
    }
  }

  @Override
  public void run() {
    try {
      this.start();
      log.log(Level.INFO, "RpcServer thread  created successful");

    } catch (TTransportException e) {
      e.printStackTrace();
    }
  }
}
