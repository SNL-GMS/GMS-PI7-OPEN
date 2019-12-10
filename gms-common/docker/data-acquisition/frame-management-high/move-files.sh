#!/bin/bash

#Vars for where and from to move files. 
#MV_FROM should be where rsync is putting files
#MV_TO should be where nifi listfile processor is looking 
[[ -z "${MV_FROM}" ]] && mvFrom='/shared-volume/rsynced-files' || mvFrom="${MV_FROM}"
[[ -z "${MV_TO}" ]] && mvTo='/shared-volume/dataframes' || mvTo="${MV_TO}"
[[ -z "${MV_FREQ}" ]] && mvFreq='5' || mvFreq="${MV_FREQ}"

while [ 1 ]
do
  #Move files from the rsync folder to where nifi is expecting them
  mv ${mvFrom}/*.inv $mvTo
  mv ${mvFrom}/*.json $mvTo
  sleep $mvFreq
done
