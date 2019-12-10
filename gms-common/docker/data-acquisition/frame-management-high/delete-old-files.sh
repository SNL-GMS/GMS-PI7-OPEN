#!/bin/bash

#Vars for file deletion,sleep threadhold, and deletion dir (should be the where nifi is configured to look (MV_TO)). 1440 seconds = 1 day
[[ -z "${DELETION_AGE_MINS}" ]] && deletionAgeMins='30' || deletionAgeMins="${DELETION_AGE_MINS}"
[[ -z "${DELETION_SLEEP}" ]] && deletionSleep='5' || deletionSleep="${DELETION_SLEEP}"
[[ -z "${MV_TO}" ]] && mvTo='/shared-volume/dataframes' || mvTo="${MV_TO}"

while [ 1 ]
do
  #Delete any files older than the configured age
  for f in `find $mvTo -type f -mmin +$deletionAgeMins -name "*.inv"`; do
    rm -f $f
  done
  for f in `find $mvTo -type f -mmin +$deletionAgeMins -name "*.json"`; do
    rm -f $f
  done
  sleep $deletionSleep
done
