#!/bin/bash

#find rawstationdataframes (.json extension) and manifest files (.inv extension)
#over 1 days old and delete them. Runs every 4 hours
#uncomment echo lines for debugging

# Run like so to daemonize
# nohup ./delete-old-files.sh 0<&- &>/dev/null &

#Read the environment variable to see which directories to hardlink to under frame-transefer
IFS=' ' read -r -a cleanup_dirs <<< "$HARDLINK_DIRECTORIES"

#Vars for file deletion and sleep threadhold. 1440 seconds = 1 day
[[ -z "${DELETION_AGE_MINS}" ]] && deletionAgeMins='1440' || deletionAgeMins="${DELETION_AGE_MINS}"
[[ -z "${DELETION_SLEEP}" ]] && deletionSleep='5' || deletionSleep="${DELETION_SLEEP}"

while [ 1 ]
do
  # for each environment-project
  for environment in "${cleanup_dirs[@]}"; do  
    for f in `find shared-volume/frame-transfer/${environment}/ -type f -mmin +$deletionAgeMins -name "*.inv"`; do
      rm -f $f
      #echo "$(date) : Deleted $f" >> logs-delete-old-files.txt
    done
    for f in `find shared-volume/frame-transfer/${environment}/ -type f -mmin +$deletionAgeMins -name "*.json"`; do
      rm -f $f
      #echo "$(date) : Deleted $f" >> logs-delete-old-files.txt
    done
  done
  sleep $deletionSleep
done
