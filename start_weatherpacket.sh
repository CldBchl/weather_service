#! /bin/bash

trap "kill 0" EXIT

# params: type sourceIP sourcePort destIP destPort

echo "Starting Weatherstations:"
# parameters Stationname IpForSensors PortForSensors IpForHttp PortForHttp locationID IpForThriftServer PortForThriftServer
./weatherstation/build/install/weatherstation/bin/weatherstation Station1  0.0.0.0 5555 0.0.0.0 5554 1 0.0.0.0 9090 &
./weatherstation/build/install/weatherstation/bin/weatherstation Station2  0.0.0.0 5565 0.0.0.0 5564 2 0.0.0.0 9090 &
./weatherstation/build/install/weatherstation/bin/weatherstation Station3  0.0.0.0 5575 0.0.0.0 5574 3 0.0.0.0 9090 &


echo "Starting Sensors:"
# params: type interval sourceIP sourcePort destIP destPort

./sensor/build/install/sensor/bin/sensor temperature 2 0.0.0.0 5556 0.0.0.0 5555 &
./sensor/build/install/sensor/bin/sensor humidity 2 0.0.0.0 5557 0.0.0.0 5555 &
./sensor/build/install/sensor/bin/sensor wind 2 0.0.0.0 5558 0.0.0.0 5555 &
./sensor/build/install/sensor/bin/sensor rain 2 0.0.0.0 5559 0.0.0.0 5555 &

./sensor/build/install/sensor/bin/sensor temperature 2 0.0.0.0 5566 0.0.0.0 5565 &
./sensor/build/install/sensor/bin/sensor humidity 2 0.0.0.0 5567 0.0.0.0 5565 &
./sensor/build/install/sensor/bin/sensor wind 2 0.0.0.0 5568 0.0.0.0 5565 &
./sensor/build/install/sensor/bin/sensor rain 2 0.0.0.0 5569 0.0.0.0 5565 &

./sensor/build/install/sensor/bin/sensor temperature 2 0.0.0.0 5576 0.0.0.0 5575 &
./sensor/build/install/sensor/bin/sensor humidity 2 0.0.0.0 5577 0.0.0.0 5575 &
./sensor/build/install/sensor/bin/sensor wind 2 0.0.0.0 5578 0.0.0.0 5575 &
./sensor/build/install/sensor/bin/sensor rain 2 0.0.0.0 5579 0.0.0.0 5575 &

wait
