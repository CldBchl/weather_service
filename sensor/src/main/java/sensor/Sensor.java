package sensor;

import java.io.IOException;
import java.net.DatagramPacket;
import java.nio.charset.StandardCharsets;
import java.util.Random;
import java.net.DatagramSocket;
import java.net.InetAddress;
import org.json.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.text.DecimalFormat;


public class Sensor{

    private String type;
    private InetAddress ip;
    private int port;
    private DatagramSocket udpSocket;
    private InetAddress remoteIp;
    private int remotePort;


    public Sensor(String type, String ip, String port, String remoteIp, String remotePort){

        this.type = type;
        this.port = Integer.parseInt(port);
        this.remotePort = Integer.parseInt(remotePort);


        try {
            this.ip = InetAddress.getByName(ip);
            this.remoteIp = InetAddress.getByName(remoteIp);
        } catch (Exception e){
            System.out.println("Enter a valid IP-Address");
            e.printStackTrace();
        }


        try {
            this.udpSocket = new DatagramSocket(this.port, this.ip);
        } catch (Exception e){
            System.out.println("Couldn't create Socket: ");
            e.printStackTrace();
        }

    }

    public void run(){
       float lastValue = 0;
        for (int i = 0; i<5; i++){
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            lastValue = generateData(lastValue);
        }
    }


    private float generateData(float value) {
        Random rand = new Random();
        DecimalFormat df = new DecimalFormat("#.##");

        float min = -1;
        float max = -1;
        String unit = "" ;

        float step = -1;
        //int lastValue = 15;


        switch (this.type) {
            case "temperature":
                min = 10;   // minimum temperature
                max = 19;   // maximum temperature
                step = 3;
                unit = "°C";
                break;

            case "humidity":
                min = 10;   // minimum humidity
                max = 70;   // maximum humidity
                step = 2;
                unit = "%";
                break;

            case "rain":
                min = 0;
                max = 10;
                step = 2;
                unit = "mm/m²";
                break;

            case "wind":
                min = 0;
                max = 45;
                step = 2;
                unit = "km/h";
                break;

                default:
                    throw new IllegalArgumentException("Invalid type" + this.type);
        }

            float delta = rand.nextFloat();

            boolean bool = rand.nextBoolean();

            if (!bool) {
                value += delta;
            } else {
                value -= delta;
            }

            if (value < min) {
                value = min + step;
            } else if (value > max) {
                value = max - step;
            }

            //System.out.println(value);


        String timeStamp = ZonedDateTime                    // Represent a moment as perceived in the wall-clock time used by the people of a particular region ( a time zone).
                .now(                            // Capture the current moment.
                        ZoneId.of( "Europe/Berlin" )  // Specify the time zone using proper Continent/Region name. Never use 3-4 character pseudo-zones such as PDT, EST, IST.
                )                                // Returns a `ZonedDateTime` object.
                .format(                         // Generate a `String` object containing text representing the value of our date-time object.
                        DateTimeFormatter.ofPattern( "uuuu.MM.dd.HH.mm.ss" )
                );                                // Returns a `String`.


        JSONObject data = new JSONObject();
        data.put("type",this.type);
        data.put("value", df.format(value));
        data.put("unit",unit);
        data.put("timestamp", timeStamp);

        byte[] bytes = data.toString().getBytes(StandardCharsets.UTF_8);
        System.out.println(data.toString());
        DatagramPacket p = new DatagramPacket(bytes, bytes.length, this.remoteIp, this.remotePort );

        try {
            this.udpSocket.send(p);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return value;
    }

}
