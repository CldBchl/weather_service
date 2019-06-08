#! /bin/bash

# This file launches a weatherservice which implements the WeatherAPI interface

trap "kill 0" EXIT

#remove any data which is stored in the weatherservice
rm -r ./serverData/

echo "Starting Weatherservice:"
# parameters Name Port
./weatherservice/build/install/weatherservice/bin/weatherservice Demo 9090 9091 9092

wait