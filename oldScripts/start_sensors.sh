#! /bin/bash

trap "kill 0" EXIT

# params: type interval sourceIP sourcePort destIP destPort

echo "Starting Sensors:"
../sensor/build/install/sensor/bin/sensor temperature 2 0.0.0.0 5556 0.0.0.0 5555 &
../sensor/build/install/sensor/bin/sensor humidity 2 0.0.0.0 5557 0.0.0.0 5555 &
../sensor/build/install/sensor/bin/sensor wind 2 0.0.0.0 5558 0.0.0.0 5555 &
../sensor/build/install/sensor/bin/sensor rain 2 0.0.0.0 5559 0.0.0.0 5555 &

wait
