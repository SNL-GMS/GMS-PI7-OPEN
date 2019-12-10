#!/usr/bin/env bash

# Exit on errors
set -e

if [ -z "$1" ]; then
  echo "Usage requires one argument of folder where data files are"
  exit 1
fi

inputDir=$1
outputDir=${inputDir}/gms_test_data_set
mkdir -p ${outputDir}

# Define workingDir at level gradle commands work at.
declare workingDir

if [ ! -z "${GMS_HOME}" ]; then
  workingDir=${GMS_HOME}
  echo "Working dir $workingDir set by environment variable GMS_HOME"
else
  scriptDir="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null && pwd )"
  workingDir=${scriptDir}/../../../../../
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

channelsFile=${outputDir}/channel.json

echo "*** Running beam definition converter ***"
gradle -p ${workingDir} :beam-converter:run -Dexec.args=\
"-beamDefinitionDir ${inputDir}/beam-definitions/  -outputDir ${outputDir}"
echo "*** Done running beam definition converter ***"

echo "*** Running qc mask converter ***"
gradle -p ${workingDir} :qc-mask-converter:run -Dexec.args=\
"-qcDir ${inputDir}/masks/ -channelsFile ${channelsFile} -outputDir ${outputDir}"
echo "*** Done running qc mask converter ***"

echo "*** Running event and signal detection converter ***"
gradle -p ${workingDir} :css-processing-converter:run -Dexec.args=\
"-outputDir ${outputDir} \
-stationsFile ${outputDir}/station.json \
-aridToWfidFile ${inputDir}/Arid2Wfid.json \
-event ${inputDir}/${commonFileName}.event \
-origin ${inputDir}/${commonFileName}.origin \
-origerr ${inputDir}/${commonFileName}.origerr \
-assoc ${inputDir}/${commonFileName}.assoc \
-arrival ${inputDir}/${commonFileName}.arrival \
-amplitude ${inputDir}/${commonFileName}.amplitude"
echo "*** Done running event and signal detection converter ***"

echo "*** Running waveform converter ***"
gradle -p ${workingDir} :css-waveform-converter:run -Dexec.args=\
"-wfDiscFile ${inputDir}/${commonFileName}.wfdisc -channelsFile ${channelsFile} -outputDir ${outputDir}"
echo "*** Done running waveform converter ***"


echo "*** Done ***"
