#!/bin/bash

set -eux

# Get the path to the directory containing this bash script.  Then define other
# paths that will be reused later.  `$GMS_HOME` is the path to the
# `gms-common/java` directory.  It needs to be exported because some of the
# scripts in the `scripts` subdirectory use it.
script_dir="$( cd "$( dirname "${BASH_SOURCE[0]}" )" > /dev/null && pwd )"
docker_build_dir="${script_dir}/docker-build"
export GMS_HOME="${script_dir}/../../../.."
standard_test_data_set_dir="${GMS_HOME}/../test-data/standard_test_data_set"

# Run the test data conversion tools.  `create-test-data-set.sh` requires that
# the CWD be set this way.
(cd ${GMS_HOME}/gms/shared/utilities/standard-test-data-set/scripts && time ./create-test-data-set.sh ${standard_test_data_set_dir})

# Ensure that the Java `waveform-loader` program is built.
time gradle -p ${GMS_HOME} :waveform-loader:build

# Copy everything that we need for the `docker build` command into the
# `docker-build` subdirectory.  Separate `rsync` commands are used here
# so we can see how long each one takes in the build output.
time rsync -a --delete ${standard_test_data_set_dir}/w ${docker_build_dir}
time rsync -a --delete ${standard_test_data_set_dir}/FkSpectra ${docker_build_dir}
time rsync -a --delete ${standard_test_data_set_dir}/feature-prediction ${docker_build_dir}
time rsync -a --delete ${standard_test_data_set_dir}/gms_test_data_set ${docker_build_dir}
time rsync -a --delete ${script_dir}/scripts ${docker_build_dir}
time rsync -a --delete ${script_dir}/Dockerfile ${docker_build_dir}

# Copy the packaged up compiled `waveform-loader` tool into the `docker-build`
# subdirectory.
rm -rf ${docker_build_dir}/waveform-loader
mkdir ${docker_build_dir}/waveform-loader
tar -C ${docker_build_dir}/waveform-loader --strip-components 1 -xf ${GMS_HOME}/gms/core/data-acquisition/css30-loader/waveform-loader/build/distributions/waveform-loader-*.tar

# Everything needed to do the `docker build` should now be in the
# `docker-build` subdirectory.
