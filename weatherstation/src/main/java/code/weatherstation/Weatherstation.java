/*
 * This is the Weatherstation class.
 */

package weatherstation;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class Weatherstation {

  //attributes
    private String name;
    private InetAddress ipAdress;
    private int port;

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
    }

  private void initializeSocket(){

    try {
      DatagramSocket socket= new DatagramSocket(port, ipAdress);
    } catch (SocketException e) {
      e.printStackTrace();
    }

  }

}
