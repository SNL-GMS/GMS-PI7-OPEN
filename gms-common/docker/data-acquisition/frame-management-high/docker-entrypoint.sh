#!/bin/bash 

mkdir -p $MV_FROM $MV_TO
chmod a+rw $MV_FROM
chmod a+rw $MV_TO

nohup ./move-files.sh 0<&- &>/dev/null &

./delete-old-files.sh

