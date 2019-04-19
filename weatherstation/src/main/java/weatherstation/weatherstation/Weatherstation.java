/*
 * This is the Weatherstation class.
 */

package weatherstation;

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
      Weatherstation myWS=
          new Weatherstation("1", "1","1");
        System.out.println(myWS.name);
    }

  private void initializeSocket(){

  }

}
