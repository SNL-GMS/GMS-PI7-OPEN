#!/usr/bin/env bash

# Exit on errors
set -e

inputDir=$1
# Check inputDir exists and is directory
if [ ! -d "${inputDir}" ]; then
  echo "${inputDir} does not exist or is not a directory"
  exit 1
fi

declare serviceHost
if [ -z "${SD_URL}" ]; then
  echo "Require variable SD_URL to be set"
  exit 1
fi

echo "Signal detection Service URL: ${SD_URL}"
url="${SD_URL}/coi/channel-segments/fks"

for file in ${inputDir}/*; do
  echo "Posting file $file to ${url}"
  curl --fail -w "\n" -i -X POST ${url} -d @${file}
done


