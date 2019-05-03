package sensor;

import java.util.logging.Level;
import java.util.logging.Logger;
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


/*
 * The Sensor class generates data and sends it via UDP
 */

public class Sensor{

    private static final Logger log = Logger.getLogger(Sensor.class.getName());
    private String type;
    private InetAddress ip;
    private int port;
    private DatagramSocket udpSocket;
    private InetAddress remoteIp;
    private int remotePort;
    private int interval;


    public Sensor(String type, String interval, String ip, String port, String remoteIp, String remotePort){

        // Assignments
        this.interval = Integer.parseInt(interval);
        this.type = type;
        this.port = Integer.parseInt(port);
        this.remotePort = Integer.parseInt(remotePort);
        try {
            this.ip = InetAddress.getByName(ip);
            this.remoteIp = InetAddress.getByName(remoteIp);
        } catch (Exception e){
            System.out.println("Enter a valid IP-Address");
            System.exit(1);
        }

        // Create socket
        try {
            this.udpSocket = new DatagramSocket(this.port, this.ip);
        } catch (Exception e){
            System.out.println("Couldn't create Socket");
            log.log(Level.WARNING, "UDP socket initialization failed");
            e.printStackTrace();
            System.exit(2);
        }
    }

    /*
     * Running-loop
     */
    public void run(){
       float value = 0;
        while (true) {
            try {
                Thread.sleep(this.interval * 1000); // seconds to ms
            } catch (InterruptedException e) {
                e.printStackTrace();
                log.log(Level.WARNING, "Thread got interrupted");
            }
            value = generateData(value);
        }
    }

    /*
     * generate Data and send it to weatherstation
     */
    private float generateData(float lastValue) {
        // Assignments
        Random rand = new Random();
        DecimalFormat df = new DecimalFormat("#.##");
        float min;
        float max;
        String unit;
        float step;

        // Configure datagenerator
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

        // Generate data with random walk
        float delta = rand.nextFloat();
        boolean bool = rand.nextBoolean();
        if (!bool) {
            lastValue += delta;
        } else {
            lastValue -= delta;
        }
        if (lastValue < min) {
            lastValue = min + step;
        } else if (lastValue > max) {
            lastValue = max - step;
        }

        // Create Timestamp
        String timeStamp = ZonedDateTime.now(ZoneId.of( "Europe/Berlin" ))
                .format(DateTimeFormatter.ofPattern( "uuuu.MM.dd.HH.mm.ss" ));

        // Create JSON
        JSONObject data = new JSONObject();
        data.put("type",this.type);
        data.put("value", df.format(lastValue));
        data.put("unit",unit);
        data.put("timestamp", timeStamp);

        // JSON -> DataPacket
        byte[] bytes = data.toString().getBytes(StandardCharsets.UTF_8);
        DatagramPacket p = new DatagramPacket(bytes, bytes.length, this.remoteIp, this.remotePort );

        // Send DataPacket
        try {
            this.udpSocket.send(p);
        } catch (IOException e) {
            e.printStackTrace();
            log.log(Level.WARNING, "UDP Socktet could not send data");
            return lastValue;
        }
        return lastValue;
    }


    /*
     * Demo-mode loop
     */
    public void demoRun(){
        float value = 0;
        int repeatLoop=5;
        while (repeatLoop>0) {
            try {
                Thread.sleep(this.interval * 1000); // seconds to ms
            } catch (InterruptedException e) {
                e.printStackTrace();
                log.log(Level.WARNING, "Thread got interrupted");
            }
            value = generateData(value);
            repeatLoop-=1;
        }
        log.log(Level.INFO, "Sensor "+type+" finished demo.");
    }

}