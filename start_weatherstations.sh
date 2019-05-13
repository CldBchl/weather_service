#! /bin/bash

trap "kill 0" EXIT

echo "Starting Weatherstations:"
# parameters Stationname IpForSensors PortForSensors IpForHttp PortForHttp locationID IpForThriftServer PortForThriftServer
./weatherstation/build/install/weatherstation/bin/weatherstation test  127.0.0.1 5555 127.0.0.1 5554 0 141.100.70.110 8080

wait
