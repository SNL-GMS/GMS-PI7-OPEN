#!/usr/bin/env bash

# Exit on errors
set -e

if [ -z "$1" ]; then
  echo "Usage requires one argument which is directory where data files are"
  exit 1
fi

inputDir=$1

declare serviceHost
if [ -z "${STATION_REF_URL}" ]; then
  echo "Require variable STATION_REF_URL to be set"
  exit 1
fi

echo "Station reference Service URL: ${STATION_REF_URL}"

declare -A fileToUrl
fileToUrl["${inputDir}/channel.json"]="${STATION_REF_URL}/coi/reference-channels"
fileToUrl["${inputDir}/network.json"]="${STATION_REF_URL}/coi/reference-networks"
fileToUrl["${inputDir}/network-memberships.json"]="${STATION_REF_URL}/coi/reference-network-memberships"
fileToUrl["${inputDir}/site.json"]="${STATION_REF_URL}/coi/reference-sites"
fileToUrl["${inputDir}/site-memberships.json"]="${STATION_REF_URL}/coi/reference-site-memberships"
fileToUrl["${inputDir}/station.json"]="${STATION_REF_URL}/coi/reference-stations"
fileToUrl["${inputDir}/station-memberships.json"]="${STATION_REF_URL}/coi/reference-station-memberships"
fileToUrl["${inputDir}/sensor.json"]="${STATION_REF_URL}/coi/reference-sensors"


# Check that all files exist
for file in "${!fileToUrl[@]}"; do
   if [ ! -f "$file" ]; then
      echo "File $file does not exist"
      exit 1
   fi
done

# Upload the files
echo "Everything looks good, uploading the files with curl"
for file in "${!fileToUrl[@]}"; do
  url=${fileToUrl[${file}]}
  echo "Posting file $file to ${url}"
  curl --fail -w "\n" -i -X POST ${url} -d @${file}
done
