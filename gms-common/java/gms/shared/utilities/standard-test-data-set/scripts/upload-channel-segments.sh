#!/usr/bin/env bash

# Exit on errors
set -e

if [ -z "$1" ] || [ -z "$2" ]; then
  echo "Usage requires two arguments: directory with segment files, directory with waveform files"
  exit 1
fi

inputDir=$1
# Check inputDir exists and is directory
if [ ! -d "${inputDir}" ]; then
  echo "${inputDir} does not exist or is not a directory"
  exit 1
fi
# Check waveformsDir exists and is directory
waveformsDir=$2
if [ ! -d "${waveformsDir}" ]; then
  echo "${waveformsDir} does not exist or is not a directory"
  exit 1
fi

if [ -z "${WAVEFORMS_URL}" ]; then
  echo "Require variable WAVEFORMS_URL to be set"
  exit 1
fi

echo "Waveforms Service URL: ${WAVEFORMS_URL}"

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

# Upload the segments
echo "*** Uploading the segments ***"

# If the ${GMS_LOAD_WAVEFORMS_WITHOUT_GRADLE} environment variable is not
# defined, use the traditional gradle-based approach to load the waveform data.
# If it is defined, then run `waveform-loader` directly without using Gradle.
# This second condition is utilized by the standard-test-data-set-loader Docker
# image that gets built automatically by a CI pipeline on `gms-common`.
if [ ! -z "${GMS_LOAD_WAVEFORMS_WITHOUT_GRADLE}" ]; then
    echo "*** Loading without Gradle ***"
    waveform-loader -segmentsDir ${inputDir} -waveformsDir $2 -hostname http://${WAVEFORMS_URL}
else
    echo "*** Loading with Gradle ***"
    gradle -p ${workingDir} :waveform-loader:run -Dexec.args=\
    "-segmentsDir ${inputDir} \
    -waveformsDir $2 \
    -hostname http://${WAVEFORMS_URL}"
fi

echo "*** Done uploading the segments ***"
