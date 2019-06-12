#!/bin/bash

pidFile='pids.pid'
servicePidFile='services.pid'
servicePortFile="./servicePorts"
httpPortFile="./httpPorts"
temp="./temp"

if [[ -f ${servicePortFile} ]];
then
  # delete pidfile
  rm ${servicePortFile}
else
  echo "port file wasn't found. Aborting..."
fi

if [[ -f ${httpPortFile} ]];
then
  # delete pidfile
  rm ${httpPortFile}
else
  echo "port file wasn't found. Aborting..."
fi

if [[ -f ${pidFile} ]];
then
  #read file in reverse with tac to kill in order sensor -> station -> service
  pids=`tac ${pidFile}`

  #kill all pids
  for pid in "${pids[@]}"
  do
    kill ${pid}
  done

  # delete pidfile
  rm ${pidFile}
else
  echo "Process file wasn't found. Aborting..."
fi

sleep 4

rm -r $temp
rm -r *tcp*

if [[ -f ${servicePidFile} ]];
then
  #read file in reverse with tac to kill in order sensor -> station -> service
  pids=`tac ${servicePidFile}`

  #kill all pids
  for pid in "${pids[@]}"
  do
    kill ${pid}
  done

  # delete pidfile
  rm ${servicePidFile}
else
  echo "Process file wasn't found. Aborting..."
fi



