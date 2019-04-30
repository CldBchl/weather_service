#! /bin/bash

trap "kill 0" EXIT

echo "Starting Weatherstations:"
# parameters Stationname IpForSensors PortForSensors IpForHttp PortForHttp
./weatherstation/build/install/weatherstation/bin/weatherstation test  127.0.0.1 5555 127.0.0.1 5554

wait
