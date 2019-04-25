#! /bin/bash

trap "kill 0" EXIT

# params: type interval sourceIP sourcePort destIP destPort

echo "Starting Sensors:"
./sensor/build/install/sensor/bin/sensor temperature 2 127.0.0.1 5556 127.0.0.1 5555 &
./sensor/build/install/sensor/bin/sensor humidity 2 127.0.0.1 5557 127.0.0.1 5555 &
./sensor/build/install/sensor/bin/sensor wind 2 127.0.0.1 5558 127.0.0.1 5555 &
./sensor/build/install/sensor/bin/sensor rain 2 127.0.0.1 5559 127.0.0.1 5555 &

wait
