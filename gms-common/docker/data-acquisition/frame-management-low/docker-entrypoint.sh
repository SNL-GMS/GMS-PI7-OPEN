#!/bin/bash

mkdir -p ${HARDLINK_DIRECTORIES}

nohup ./hardlink-files.sh 0<&- &>/dev/null &

./delete-old-files.sh
