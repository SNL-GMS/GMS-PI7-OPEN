#!/bin/bash

# If there are no working/loading scripts in this directory,
# then the load_opt script in ../src fails (the for loop
# returns "no such file or directory" for /opt/*.sh)
# and the docker build breaks. With the check below, it
# gaurantees at least one script is in this directory.

if [ -z "$(ls -A -I verify_scripts.sh .)" ]; then
   echo "nothing to do"
fi
