package sensor;
public class App {
    public static void main(String[] args) {
        // params: type interval sourceIP sourcePort destIP destPort
        Sensor snsr = new Sensor(args[0],args[1],args[2],args[3],args[4], args[5]);
        snsr.run();
    }
}
