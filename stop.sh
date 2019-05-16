#!/bin/bash

pidFile='pids.pid'

if [[ -f ${pidFile} ]];
then
  #read file in reverse with tuc to kill in order sensor -> station -> service
  pids=`cat ${pidFile}`

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