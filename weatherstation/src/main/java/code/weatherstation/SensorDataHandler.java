package code.weatherstation;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SensorDataHandler implements Runnable{

  private static InetAddress receiveIpAddress;
  private static int receivePort;
  private static DatagramSocket udpSocket;
  private static final Logger log = Logger.getLogger( SensorDataHandler.class.getName() );

  public SensorDataHandler(int receivePort, InetAddress receiveIpAddress){
    try {
      udpSocket = new DatagramSocket(receivePort, receiveIpAddress);
      udpSocket.setReceiveBufferSize(1024);
      log.log(Level.INFO, "succesful receive socket creation");
    } catch (SocketException e) {
      e.printStackTrace();
      log.log(Level.WARNING, "Receive socket initialization failed");
    }

  }

  private static void receiveUDPPackets(){
    byte[] buf = new byte[1024];
    DatagramPacket packet = new DatagramPacket(buf, 1024);

    //wait for incoming UDP package
    while (true) {
      try {

        udpSocket.receive(packet);

        // Empfänger auslesen
        InetAddress address = packet.getAddress();
        int         port    = packet.getPort();
        int         len     = packet.getLength();
        byte[]      data    = packet.getData();

        System.out.printf( "Anfrage von %s vom Port %d mit der Länge %d:%n%s%n",
            address, port, len, new String( data, 0, len ) );
      }
      catch (IOException e) {
        e.printStackTrace();
        System.out.println("Error when receiving UDP packet");
        log.log(Level.WARNING, "UDP error when receiving package");

      }
    }
  }

  private static void parseAndStoreSensorData(){

    //TODO: implement method
  }

  @Override
  public void run() {
    log.log(Level.INFO, "sensorDataHandler thread successful");
    receiveUDPPackets();

  }
}
