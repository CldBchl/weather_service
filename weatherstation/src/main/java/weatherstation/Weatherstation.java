/*
 * This is the Weatherstation class.
 */

public class Weatherstation {

  //attributes
    private String name;
    private String ipAdress;
    private String port;

    public Weatherstation(String n, String i, String p) {
      name = n;
      ipAdress = i;
      port=p;
    }

  public static void main (String [] args)
    {
      Weatherstation myWS= new Weatherstation(args[0],args[1],args[2]);

    }


}
