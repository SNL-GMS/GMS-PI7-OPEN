#!/usr/bin/env bash

# Exit on errors
set -e

if [ -z "$1" ]; then
  echo "Usage requires one argument which is location of the input file"
  exit 1
fi

declare serviceHost
if [ -z "${SD_URL}" ]; then
  echo "Require variable SD_URL variable to be set"
  exit 1
fi

echo "Signal detection Service URL: ${SD_URL}"

# Check file exists
file=$1
if [ ! -f "$file" ]; then
      echo "File $file does not exist"
      exit 1
fi
url="${SD_URL}/coi/events"

echo "Posting file $file to ${url}"
curl --fail -w "\n" -i -X POST ${url} -d @${file}