package sensor;

import java.text.SimpleDateFormat;
import java.util.Random;
import java.net.DatagramSocket;
import java.net.InetAddress;
import org.json.*;
import java.util.Date;

public class Sensor implements  {

    private String type;
    private String unit;
    private InetAddress ip;
    private int port;
    private DatagramSocket socket;

    public Sensor(String type, String ip, String port, String remoteIp, String remotePort){
        this.type = type;
        try {
            this.ip = InetAddress.getByName(ip);
        } catch (Exception e){
            System.out.println("Invalid ip-address!!: ");
            e.printStackTrace();
        }

        this.port = Integer.parseInt(port);

        socket = initializeSocket(this.ip, this.port);

        try {
            socket.connect(InetAddress.getByName(remoteIp),Integer.parseInt(remoteIp));
        } catch (Exception e){
            System.out.println("Connection failed ");
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

           return new DatagramSocket(port, ip);

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

        String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());

        JSONObject data = new JSONObject();
        data.put("type","temperature");
        data.put("value", lastValue);
        data.put("unit","°C");
        data.put("timestamp", timeStamp);
       // this.socket.send();

        //https://stackoverflow.com/questions/23068676/how-to-get-current-timestamp-in-string-format-in-java-yyyy-mm-dd-hh-mm-ss
    }

    private void sendData(){

    }
}
