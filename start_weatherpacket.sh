#! /bin/bash

trap "kill 0" EXIT

# params: type sourceIP sourcePort destIP destPort

echo "Starting Weatherstations:"
# parameters Stationname IpForSensors PortForSensors IpForHttp PortForHttp
./weatherstation/build/install/weatherstation/bin/weatherstation test  127.0.0.1 5555 127.0.0.1 5554 &
./weatherstation/build/install/weatherstation/bin/weatherstation test1  127.0.0.1 5565 127.0.0.1 5564 &
./weatherstation/build/install/weatherstation/bin/weatherstation test2  127.0.0.1 5575 127.0.0.1 5574 &


echo "Starting Sensors:"
# params: type interval sourceIP sourcePort destIP destPort

./sensor/build/install/sensor/bin/sensor temperature 2 127.0.0.1 5556 127.0.0.1 5555 &
./sensor/build/install/sensor/bin/sensor humidity 2 127.0.0.1 5557 127.0.0.1 5555 &
./sensor/build/install/sensor/bin/sensor wind 2 127.0.0.1 5558 127.0.0.1 5555 &
./sensor/build/install/sensor/bin/sensor rain 2 127.0.0.1 5559 127.0.0.1 5555 &

./sensor/build/install/sensor/bin/sensor temperature 2 127.0.0.1 5566 127.0.0.1 5565 &
./sensor/build/install/sensor/bin/sensor humidity 2 127.0.0.1 5567 127.0.0.1 5565 &
./sensor/build/install/sensor/bin/sensor wind 2 127.0.0.1 5568 127.0.0.1 5565 &
./sensor/build/install/sensor/bin/sensor rain 2 127.0.0.1 5569 127.0.0.1 5565 &

./sensor/build/install/sensor/bin/sensor temperature 2 127.0.0.1 5576 127.0.0.1 5575 &
./sensor/build/install/sensor/bin/sensor humidity 2 127.0.0.1 5577 127.0.0.1 5575 &
./sensor/build/install/sensor/bin/sensor wind 2 127.0.0.1 5578 127.0.0.1 5575 &
./sensor/build/install/sensor/bin/sensor rain 2 127.0.0.1 5579 127.0.0.1 5575 &

wait