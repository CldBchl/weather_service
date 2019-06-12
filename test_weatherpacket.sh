#! /bin/bash
# This file launches a weatherstation and four sensors, which send their data to the weatherstation
# The sensors run in demo mode, this means that each one will only send five data records

trap "kill 0" EXIT

#remove files from previous test runs
rm -r ./sensorData/

echo "Starting Weatherstations:"
# parameters Stationname IpForSensors PortForSensors IpForHttp PortForHttp locationID IpForThriftServer PortForThriftServer portFile
./weatherstation/build/install/weatherstation/bin/weatherstation Demo0  127.0.0.1 5555 127.0.0.1 5554 0 0.0.0.0 9090 ./testPorts &
./weatherstation/build/install/weatherstation/bin/weatherstation Demo1  127.0.0.1 5565 127.0.0.1 5564 1 0.0.0.0 9091 ./testPorts &
./weatherstation/build/install/weatherstation/bin/weatherstation Demo2  127.0.0.1 5575 127.0.0.1 5574 2 0.0.0.0 9092 ./testPorts &

echo "Starting Sensors:"
# params: type interval sourceIP sourcePort destIP destPort

./sensor/build/install/sensor/bin/sensor temperature 5 127.0.0.1 5556 127.0.0.1 5555 demo &
./sensor/build/install/sensor/bin/sensor humidity 5 127.0.0.1 5557 127.0.0.1 5555 demo &
./sensor/build/install/sensor/bin/sensor wind 5 127.0.0.1 5558 127.0.0.1 5555 demo &
./sensor/build/install/sensor/bin/sensor rain 5 127.0.0.1 5559 127.0.0.1 5555 demo&

./sensor/build/install/sensor/bin/sensor temperature 5 127.0.0.1 5566 127.0.0.1 5565 demo &
./sensor/build/install/sensor/bin/sensor humidity 5 127.0.0.1 5567 127.0.0.1 5565 demo &
./sensor/build/install/sensor/bin/sensor wind 5 127.0.0.1 5568 127.0.0.1 5565 demo &
./sensor/build/install/sensor/bin/sensor rain 5 127.0.0.1 5569 127.0.0.1 5565 demo &

./sensor/build/install/sensor/bin/sensor temperature 5 127.0.0.1 5576 127.0.0.1 5575 demo &
./sensor/build/install/sensor/bin/sensor humidity 5 127.0.0.1 5577 127.0.0.1 5575 demo &
./sensor/build/install/sensor/bin/sensor wind 5 127.0.0.1 5578 127.0.0.1 5575 demo &
./sensor/build/install/sensor/bin/sensor rain 5 127.0.0.1 5579 127.0.0.1 5575 demo &

wait