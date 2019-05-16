#! /bin/bash

trap "kill 0" EXIT

# params: type sourceIP sourcePort destIP destPort

echo "Starting Weatherstations:"
# parameters Stationname IpForSensors PortForSensors IpForHttp PortForHttp
rm -r ./sensorData/Demo1
./weatherstation/build/install/weatherstation/bin/weatherstation Demo1  127.0.0.1 5555 127.0.0.1 5554&


echo "Starting Sensors:"
# params: type interval sourceIP sourcePort destIP destPort

./sensor/build/install/sensor/bin/sensor temperature 2 0.0.0.0 5556 0.0.0.0 5555 demo &
./sensor/build/install/sensor/bin/sensor humidity 2 0.0.0.0 5557 0.0.0.0 5555 demo &
./sensor/build/install/sensor/bin/sensor wind 2 0.0.0.0 5558 0.0.0.0 5555 demo &
./sensor/build/install/sensor/bin/sensor rain 2 0.0.0.0 5559 0.0.0.0 5555 demo&

wait
