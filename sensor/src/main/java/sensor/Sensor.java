package sensor;

import java.io.IOException;
import java.net.DatagramPacket;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Random;
import java.net.DatagramSocket;
import java.net.InetAddress;
import org.json.*;
import java.time.*;
import java.time.format.DateTimeFormatter;



public class Sensor{

    private String type;
    private String unit;
    private InetAddress ip;
    private int port;
    private DatagramSocket socket;
    private InetAddress remoteIp;
    private int remotePort;


    public Sensor(String type, String ip, String port, String remoteIp, String remotePort){
        this.type = type;
        this.remotePort = Integer.parseInt(remotePort);

        try {
            this.ip = InetAddress.getByName(ip);

            this.remoteIp = InetAddress.getByName(remoteIp);

        } catch (Exception e){
            System.out.println("Invalid ip-address!!: ");
            e.printStackTrace();
        }

        this.port = Integer.parseInt(port);


        this.socket = initializeSocket(this.ip, this.port);

        try {
            this.socket.connect(InetAddress.getByName(remoteIp),Integer.parseInt(remoteIp));

        } catch (Exception e){
            System.out.println("Connection failed ");
            e.printStackTrace();
        }

        try {
            this.ip = InetAddress.getByName(remoteIp);
        } catch (Exception e){
            System.out.println("Invalid ip-address!!: ");
            e.printStackTrace();
        }
    }

    public void run(){
        for (int i = 0; i<10; i++){
            generateData();
        }
    }

    private DatagramSocket initializeSocket(InetAddress ip, int port){
       try {


           this.socket = new DatagramSocket(port, ip);

           return socket;


       } catch (Exception e){
           System.out.println("Couldn't create Socket: ");
           e.printStackTrace();
       }
       return null;
    }

    private void generateData() {
        Random rand = new Random();

        int minHumidity = 10;
        int maxHumidity = 70;
        int minRain = 0;
        int maxRain = 10;
        int minWind = 0;
        int maxWind = 45;
        int maxTemp = 19;
        int minTemp = 10;
        int step = -1;
        int lastValue = 0;

        if (this.type.equals("temperature")) {
            int min = minTemp;
            int max = maxTemp;
            step = 2;
        }

        for(int i = 0; i < 100; i++) {

            int delta = rand.nextInt(step);
            boolean bool = rand.nextBoolean();

            if (!bool) {
                lastValue += delta;
            } else {
                lastValue -= delta;
            }

            if (lastValue < minTemp) {
                lastValue = minTemp + 1;
            } else if (lastValue > maxTemp) {
                lastValue = maxTemp - 1;
            }

            System.out.println(lastValue);
        }

        String timeStamp = ZonedDateTime                    // Represent a moment as perceived in the wall-clock time used by the people of a particular region ( a time zone).
                .now(                            // Capture the current moment.
                        ZoneId.of( "Africa/Tunis" )  // Specify the time zone using proper Continent/Region name. Never use 3-4 character pseudo-zones such as PDT, EST, IST.
                )                                // Returns a `ZonedDateTime` object.
                .format(                         // Generate a `String` object containing text representing the value of our date-time object.
                        DateTimeFormatter.ofPattern( "uuuu.MM.dd.HH.mm.ss" )
                );                                // Returns a `String`.


        JSONObject data = new JSONObject();
        data.put("type","temperature");
        data.put("value", lastValue);
        data.put("unit","°C");
        data.put("timestamp", timeStamp);

        byte[] bytes = data.toString().getBytes(StandardCharsets.UTF_8);
        System.out.println(data.toString());
        DatagramPacket p = new DatagramPacket(bytes, bytes.length, this.remoteIp, this.remotePort );

        try {
            this.socket.send(p);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendData(){

    }
}
