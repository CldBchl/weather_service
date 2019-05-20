#! /bin/bash
# This file launches a weatherstation and four sensors, which send their data to the weatherstation
# The sensors run in demo mode, this means that each one will only send five data records

trap "kill 0" EXIT

#remove files from previous test runs
rm -r ./sensorData/Demo1

echo "Starting Weatherstations:"
# parameters Stationname IpForSensors PortForSensors IpForHttp PortForHttp locationID IpForThriftServer PortForThriftServer
./weatherstation/build/install/weatherstation/bin/weatherstation Demo1  127.0.0.1 5555 127.0.0.1 5554 0 0.0.0.0 9090 &


echo "Starting Sensors:"
# params: type interval sourceIP sourcePort destIP destPort

./sensor/build/install/sensor/bin/sensor temperature 2 127.0.0.1 5556 127.0.0.1 5555 demo &
./sensor/build/install/sensor/bin/sensor humidity 2 127.0.0.1 5557 127.0.0.1 5555 demo &
./sensor/build/install/sensor/bin/sensor wind 2 127.0.0.1 5558 127.0.0.1 5555 demo &
./sensor/build/install/sensor/bin/sensor rain 2 127.0.0.1 5559 127.0.0.1 5555 demo&

wait