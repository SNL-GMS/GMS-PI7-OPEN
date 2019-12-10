#!/usr/bin/env bash

# Exit on errors
set -e

git clone git@gitlab.xxxxx.xxx:gms/shared/test_data/standard_test_data_set.git
rm -rf standard_test_data_set/.git

inputDir=$(pwd)/standard_test_data_set
outputDir=${inputDir}/gms_test_data_set
mkdir -p ${outputDir}

# Define workingDir at level gradle commands work at.
declare workingDir
workingDir=../../../../../

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

mv ${outputDir}/station.json ./
mv ${outputDir}/signal-detections.json ./
mv ${outputDir}/events.json ./

rm -rf standard_test_data_set