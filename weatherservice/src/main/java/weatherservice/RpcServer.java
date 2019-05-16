package weatherservice;

import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;
import org.apache.thrift.transport.TTransportException;
import weatherservice.thrift.Weather;
import java.net.ServerSocket;
import java.nio.channels.Selector;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * The RpcServer class
 */
public class RpcServer implements Runnable {

    private static final Logger log = Logger.getLogger(RpcServer.class.getName());
    private int serverPort;
    private TThreadPoolServer server;

    public RpcServer(int port) {
                 serverPort = port;
    }

    public void start() throws TTransportException {
        TServerTransport serverTransport = new TServerSocket(serverPort);
        server = new TThreadPoolServer(new TThreadPoolServer.Args(serverTransport)
                .processor(new Weather.Processor<>(new WeatherServiceImpl("start"))));

        log.log(Level.INFO, "Starting server");

        server.serve();

        log.log(Level.INFO, "Server started");
    }

    public void stop(){
        if (server != null && server.isServing()) {

            log.log(Level.INFO, "Stopping server");

            server.stop();

            log.log(Level.INFO,"Server stopped");
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
