#!/bin/bash

set -eux

# Get the path to the directory containing this bash script.  Then define other
# paths that will be reused later.  `$GMS_HOME` is the path to the
# `gms-common/java` directory.  It needs to be exported because some of the
# scripts in the `scripts` subdirectory use it.
script_dir=""$( cd "$( dirname "${BASH_SOURCE[0]}" )" > /dev/null && pwd)""
docker_build_dir="${script_dir}"
export GMS_HOME="${script_dir}/../../java"

# Run the test data conversion tools.  `create-test-data-set.sh` requires that
# the CWD be set this way.
( cd $script_dir && time ./gen_station_ref.sh data station_ref_data )
