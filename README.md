# Weather service

## Project description 

This project was part of the distributed systems lab during the summer semester 2019 at Hochschule Darmstadt, University of Applied Sciences.
The task was to implement a distributed weather service consisting of 
- sensors (temperature, humidity, rain, wind) : sensors send randomly generated weather data via UDP/ MQTT to their respective weather stations
- weather stations: receive weather data from at least three sensors. Weather stations include a HTTP- server 
which displays the received sensor data via a REST API. Furthermore, the weather stations transmit the aggregated sensor
data via Thrift to a connected weather service.
- weather services: weather services gather data from multiple weather stations. They implement a master slave 
architecture and consist of at least three replicated servers which synchronize the received data continuously.  

![Project overview](project_overview.png) 


## Build the project

gradle installDist

## Demo run

Launch the demo mode to run the application on your host system. 

`docker-compose up`

**Note**: if you run the code on macOS, remove the last line from the docker-compose.yml (**network_mode: host**)



`./test_weatherservice.sh`

This script launches a weather service instance with three servers.


`./test_weatherpacket.sh`

This script launches three weather stations and for each sensors it launches four sensors, that 
send their data to the station. Each weather station transmits its data to the server of the weather
service it is assigned to. 


Check the transmitted sensor values on ports **5554**, **5564**, **5574** according to the [REST API](##REST) documentation.

`docker-compose down`

## Run

`docker-compose up`

**Note**: if you run the code on macOS, remove the last line from the docker-compose.yml (**network_mode: host**)


Update the start.sh file:
- replace *ip0=0.0.0.0* with your local IP address
- replace *ip1=0.0.0.0* with a remote IP address you want to communicate with

The start.sh script will launch a weather service instance, three weather stations and their respective sensors. 

`./start.sh`

`./stop.sh`

`docker-compose down`

## Weather stations REST API 

### GET

sensors/`sensortype`/`whichdata`

**sensortypes:** 

- temperature
- rain
- wind
- humidity

**whichdata:** 

- current
- history
        
