#!/bin/bash

if [ ! -d ${RSYNC_LOCAL_DIR} ]; then
  mkdir -p ${RSYNC_LOCAL_DIR}
fi

/usr/local/bin/rsync-transfer.sh
