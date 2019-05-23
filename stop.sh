#!/bin/bash

pidFile='pids.pid'
servicePidFile='services.pid'

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

sleep 5

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