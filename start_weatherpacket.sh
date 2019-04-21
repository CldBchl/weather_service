#! /bin/bash

trap "kill 0" EXIT

# params: type sourceIP sourcePort destIP destPort

echo "Starting Weatherstations:"
# parameters Stationname IpForSensors PortForSensors IpForHttp PortForHttp
./weatherstation/build/install/weatherstation/bin/weatherstation test  127.0.0.1 5555 127.0.0.1 5556 &


echo "Starting Sensors:"
# params: type sourceIP sourcePort destIP destPort

./sensor/build/install/sensor/bin/sensor temperature 127.0.0.1 5556 127.0.0.1 5555 &
./sensor/build/install/sensor/bin/sensor humidity 127.0.0.1 5557 127.0.0.1 5555 &
./sensor/build/install/sensor/bin/sensor wind 127.0.0.1 5558 127.0.0.1 5555 &
./sensor/build/install/sensor/bin/sensor rain 127.0.0.1 5559 127.0.0.1 5555 &

wait
