#!/usr/bin/env bash

# Exit on errors
set -e

if [ $# -ne 2 ]; then
  echo "Usage requires two arguments: ./gen_station_ref.sh <inputDir> <outputDir>"
  exit 1
fi

inputDir=$(pwd)/$1
outputDir=$(pwd)/$2

# Define workingDir at level gradle commands work at.
declare workingDir

if [ ! -z "${GMS_HOME}" ]; then
  workingDir=${GMS_HOME}
  echo "Working dir $workingDir set by environment variable GMS_HOME"
else
  scriptDir="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null && pwd )"
  echo "Working dir $workingDir set as relative to this script"
fi

commonFileName=ueb_test

echo "*** Running station reference converter ***"
gradle -p ${workingDir} :css-stationref-converter:run -Dexec.args=\
"-outputDir ${outputDir} \
-affiliation ${inputDir}/${commonFileName}.affiliation \
-instrument ${inputDir}/${commonFileName}.instrument \
-network ${inputDir}/${commonFileName}.network \
-sensor ${inputDir}/${commonFileName}.sensor \
-site ${inputDir}/${commonFileName}.site \
-sitechan ${inputDir}/${commonFileName}.sitechan"
echo "*** Done running station reference converter ***"
