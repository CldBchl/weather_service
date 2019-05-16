#! /bin/bash

trap "kill 0" EXIT

# params: type sourceIP sourcePort destIP destPort

echo "Starting Weatherstations:"
# parameters Stationname IpForSensors PortForSensors IpForHttp PortForHttp locationID IpForThriftServer PortForThriftServer
rm -r ./sensorData/Demo1
./weatherstation/build/install/weatherstation/bin/weatherstation Demo1  127.0.0.1 5555 127.0.0.1 5554 1 141.100.70.110 8080 &


echo "Starting Sensors:"
# params: type interval sourceIP sourcePort destIP destPort

./sensor/build/install/sensor/bin/sensor temperature 2 127.0.0.1 5556 127.0.0.1 5555 demo &
./sensor/build/install/sensor/bin/sensor humidity 2 127.0.0.1 5557 127.0.0.1 5555 demo &
./sensor/build/install/sensor/bin/sensor wind 2 127.0.0.1 5558 127.0.0.1 5555 demo &
./sensor/build/install/sensor/bin/sensor rain 2 127.0.0.1 5559 127.0.0.1 5555 demo&

wait
