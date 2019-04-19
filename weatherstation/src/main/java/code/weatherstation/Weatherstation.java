/*
 * This is the Weatherstation class.
 */

package weatherstation;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class Weatherstation {

  //attributes
    private String name;
    private static InetAddress ipAdress;
    private static int port;
    private static DatagramSocket socket;

    public Weatherstation(String n, String i, String p) {
      name = n;
      try {
        ipAdress = InetAddress.getByName(i);
      } catch (UnknownHostException e) {
        System.out.println("Please enter a valid IP-Address");
      }
      port=Integer.parseInt(p);
    }

  public static void main (String [] args)
    {
      Weatherstation myWS=
          new Weatherstation(args[0], args[1], args[2]);
        System.out.println(myWS.name);

        initializeSocket();

        byte[] buf = new byte[1024];
        DatagramPacket packet = new DatagramPacket(buf, 1024);

      //wait for incoming UDP package
      while (true) {
        try {
          socket.receive(packet);
        } catch (IOException e) {
          e.printStackTrace();
        }
      }

    }

  private static void initializeSocket(){

    try {
      socket= new DatagramSocket(port, ipAdress);
      socket.setReceiveBufferSize(1024);
    } catch (SocketException e) {
      e.printStackTrace();
    }

  }

}
