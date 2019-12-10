#!/usr/bin/env bash

# Exit on errors
set -e

if [ -z "$1" ] || [ -z "$2" ] || [ -z "$3" ]; then
  echo "Usage: <directory of GMS data files e.g. gms_test_data_set> <fk files directory> <waveform files directory>"
  exit 1
fi

check_dir_exists () {
if [ ! -d "${1}" ]; then
  echo "${1} does not exist or is not directory"
  exit 1
fi
}

inputDir=$1
check_dir_exists ${inputDir}
fkDir=$2
check_dir_exists ${fkDir}
waveformsDir=$3
check_dir_exists ${waveformsDir}

# Load station reference
echo "*** Loading station reference"
bash upload-station-reference.sh ${inputDir}
echo "*** Done loading station reference"

# TODO: load state-of-health

# Load QC masks
echo "*** Loading QC masks"
bash upload-qc-masks.sh ${inputDir}/converted-qc-masks.json
echo "*** Done loading QC masks"

# Load FK's
echo "Loading FK's"
bash upload-fk.sh ${fkDir}
echo "Done loading FK's"

# Load events
echo "*** Loading events"
bash upload-events.sh ${inputDir}/events.json
echo "Done loading events"

# Load signal detections
echo "*** Loading signal detections"
bash upload-signal-detections.sh ${inputDir}/signal-detections.json
echo "*** Done loading signal detections"

# Load channel segments (waveforms)
echo "Loading channel segments (waveforms)"
bash upload-channel-segments.sh ${1}/segments-and-soh ${waveformsDir}
echo "Done loading channel segments (waveforms)"

echo "*** Done uploading the test data set"