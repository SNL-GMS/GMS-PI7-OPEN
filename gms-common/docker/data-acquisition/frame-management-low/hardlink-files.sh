#!/usr/bin/env bash

# Run like so to daemonize
# nohup ./hardlink-files.sh 0<&- &>/dev/null &

#Read the environment variable to see which directories to hardlink to under frame-transefer
IFS=' ' read -r -a hardlink_dirs <<< "$HARDLINK_DIRECTORIES"

while [ 1 ]
do
  # find specific filenames to not hardlink temp files
  for file in `find shared-volume/dataframes -name "*.json" -o -name "*.inv"`; do
    # for each environment-project
    for environment in "${hardlink_dirs[@]}"; do
      # create a hardlink from the file to a file of the same name in the target folder
      fileName=$(basename ${file})
      target=shared-volume/frame-transfer/${environment}/${fileName}
      ln ${file} ${target}
    done
    # remove the original file
    rm -f ${file}
  done
  sleep 1
done
