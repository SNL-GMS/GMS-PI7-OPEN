#!/usr/bin/env bash

# Exit on errors
set -e

if [ -z "$1" ] || [ -z "$2" ]; then
  echo "Usage: load-api-gateway.sh <SOURCE_DIR> <TARGET_DIR>"
  echo "Example: load-api-gateway.sh /standard-test-data-set /api-gateway-volume/Standard_Test_Data"
  exit 1
fi

check_dir_exists () {
if [ ! -d "${1}" ]; then
  echo "Source directory ${1} does not exist or is not directory"
  exit 1
fi
}

source_dir=${1}
target_dir=${2}

# Ensure that the source directory and the necessary subdirectories exist
check_dir_exists "${source_dir}/w"
check_dir_exists "${source_dir}/FkSpectra"
check_dir_exists "${source_dir}/feature-prediction"
check_dir_exists "${source_dir}/gms_test_data_set"

# Ensure that the target directory does *not* exist (yet).
if [ -e "${target_dir}" ]; then
  echo "Target directory ${target_dir} already exists. This script will now exit with an error to avoid overwriting something important."
  exit 1
fi

set -x
mkdir -p ${target_dir}
rsync -a \
    ${source_dir}/w \
    ${source_dir}/FkSpectra \
    ${source_dir}/feature-prediction \
    ${source_dir}/gms_test_data_set \
    ${target_dir}
