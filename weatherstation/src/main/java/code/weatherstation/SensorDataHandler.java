package code.weatherstation;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * The SensorDataHandler class receives data via UDP and processes the incoming messages.
 */

public class SensorDataHandler implements Runnable{

  private static final Logger log = Logger.getLogger( SensorDataHandler.class.getName() );
  private static DatagramSocket udpSocket;

  public SensorDataHandler(int receivePort, InetAddress receiveIpAddress){
    try {
      udpSocket = new DatagramSocket(receivePort, receiveIpAddress);
      udpSocket.setReceiveBufferSize(1024);
      log.log(Level.INFO, "Successful UDP socket creation");
    } catch (SocketException e) {
      e.printStackTrace();
      log.log(Level.WARNING, "UDP socket initialization failed");
      //TODO: handle error
      return;
    }

  }

  private static void handleSensorData(){

    receiveUDPPackets();
    parseAndStoreSensorData();

  }


  private static void receiveUDPPackets(){
    byte[] buf = new byte[1024];
    DatagramPacket packet = new DatagramPacket(buf, 1024);

    //wait for incoming UDP package
    while (true) {
      try {

        udpSocket.receive(packet);

        InetAddress address = packet.getAddress();
        int         port    = packet.getPort();
        int         len     = packet.getLength();
        byte[]      data    = packet.getData();

        System.out.printf( "Receive data from IP %s and from port %d :%n%s%n",
            address, port, new String( data, 0, len ) );
      }
      catch (IOException e) {
        e.printStackTrace();
        System.out.println("Error when receiving UDP packet");
        log.log(Level.WARNING, "Error when UDP receiving package");

      }
    }
  }

  private static void parseAndStoreSensorData(){

    //TODO: implement method
  }

  @Override
  public void run() {
    log.log(Level.INFO, "sensorDataHandler thread successful");
    handleSensorData();

  }
}
