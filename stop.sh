#!/bin/bash

pidFile='pids.pid'

if [[ -f ${pidFile} ]];
then
<<<<<<< HEAD
  #read file in reverse with tac to kill in order sensor -> station -> service
  pids=`tac ${pidFile}`
=======
  #read file in reverse with tuc to kill in order sensor -> station -> service
  pids=`cat ${pidFile}`
>>>>>>> 4caba1c2636809931c265c58b262edf71fb1d053

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