#! /bin/bash

# paths to binaries
serviceBin=./weatherservice/build/install/weatherservice/bin/weatherservice
stationBin=./weatherstation/build/install/weatherstation/bin/weatherstation
sensorBin=./sensor/build/install/sensor/bin/sensor

# path for pidfile
servicePidFile="./services.pid"
pidFile="./pids.pid"
servicePortFile="./servicePorts"
httpPortFile="./httpPorts"

# define names of services and stations as well as types of sensors.
# each service will spawn the specified stations and each station spawns each sensorType
services=( Service1 )
stations=( Station1 Station2 Station3 )
sensors=( temperature humidity wind rain )

# counter for location ids
locationId=0

# interval in seconds in which sensors will generate data
sensorInterval=2

# net configuration
port=8000

# ip0 is local ip
ip0=0.0.0.0

#ip1 set ip1 as target ip, if you want to send data to another pc
ip1=0.0.0.0

# counter for ServicePortArray
ServicePortCounter=0

# saves pids of spawned programms in a pidfile to kill later
if [[ -f ${pidFile} ]];
then
  echo "$pidFile already exists. Stop the process before attempting to start."
else
  echo -n "" > ${pidFile}

if [[ -f ${servicePidFile} ]];
then
  echo "$servicePidFile already exists. Stop the process before attempting to start."
  rm ${pidFile}
else
  echo -n "" > ${pidFile}

if [[ -f ${servicePortFile} ]];
then
  rm ${servicePortFile}
else
  echo -n "" > ${servicePortFile}
fi

if [[ -f ${httpPortFile} ]];
then
  rm ${httpPortFile}
else
  echo -n "" > ${httpPortFile}
fi

  # Starting local services
  echo "Starting Weatherservices"
  servicePorts=( )
  for service in ${services[@]} ; do
    # port for stations xxxx
    servicePort1=${port}
    servicePorts+=( ${servicePort1} )
    echo ${port} >> ${servicePortFile}
    port=$((port+1))
    servicePort2=${port}
    servicePorts+=( ${servicePort2} )
    echo ${port} >> ${servicePortFile}
    port=$((port+1))
    servicePort3=${port}
    servicePorts+=( ${servicePort3} )
    echo ${port} >> ${servicePortFile}

    # params: serviceName, port1, port2, port3
    ${serviceBin} ${service} ${servicePort1} ${servicePort2} ${servicePort3} &

    # save pid in file
    echo -n "$! " >> ${servicePidFile}

    # offset port so station-sensor-combinations are equal
    port=$((port+10))

    echo "Starting Weatherstations for ${service} :"
    servicePort=0
    for station in ${stations[@]} ; do
      #stationPorts xxY0
      stationPort=${port}

      # http port of station: xxY1
      port=$((port+1))
      echo ${port} >> ${httpPortFile}

      if [[ ${ServicePortCounter} = ${#servicePorts[@]} ]];
      then
        ServicePortCounter=0
      fi
      servicePort=$((servicePorts[ServicePortCounter]))
      ServicePortCounter=$((ServicePortCounter+1))

      #echo $servicePort
      #echo $servicePort1
      #echo $servicePort2
      # params stationname IpForSensors PortForSensors IpForHttp PortForHttp locationID IpForThriftServer PortForThriftServer
      ${stationBin} ${station} ${ip0} ${stationPort} ${ip0} ${port} ${locationId} ${ip1} ${servicePort} ${servicePortFile} &
       # save pid in file
      echo  "$! " >> ${pidFile}

      # increment locationCounter
      locationId=$((locationId+1))

      echo "Starting Sensors for ${station} :"
      for sensor in ${sensors[@]} ; do
      echo "Starting Sensor ${sensor} :"
        # ports xxY2 - xxY5
        port=$((port+1))

        # params: type interval sourceIP sourcePort destIP destPort
        ${sensorBin} ${sensor} ${sensorInterval} ${ip0} ${port} ${ip1} ${stationPort} &
         # save pid in file
        echo  "$! " >> ${pidFile}

      done
      # offset port, so next station starts at x x Y+1 0
      port=$((port+5))
    done
  # offset
  port=$((port+10))
  done
  echo "done"

fi

fi