# Weatherservice

## Build

gradle installDist

## Run

docker-compose up 

./start.sh

./stop.sh

docker-compose down 

## Rest

### GET

sensors/`sensortype`/`whichdata`

sensortypes: 

- temperature
- rain
- wind
- humidity

whichdata: 

- current
- history
        