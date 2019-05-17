#! /bin/bash

trap "kill 0" EXIT

echo "Starting Weatherstations:"
# parameters Stationname IpForSensors PortForSensors IpForHttp PortForHttp
../weatherstation/build/install/weatherstation/bin/weatherstation test  0.0.0.0 5555 0.0.0.0 5554

wait
