#!/bin/bash

# Update the config
CONFIG="${ZOO_CONF_DIR}/zoo.cfg"

sed -i "/tickTime/c\tickTime=${ZOO_TICK_TIME}" ${CONFIG}
sed -i "/initLimit/c\initLimit=${ZOO_INIT_LIMIT}" ${CONFIG}
sed -i "/syncLimit/c\syncLimit=${ZOO_SYNC_LIMIT}" ${CONFIG}
sed -i "/dataDir/c\dataDir=${ZOO_DATA_DIR}" ${CONFIG}
sed -i "/clientPort/c\clientPort=${ZOO_PORT}" ${CONFIG}

for server in ${ZOO_SERVERS}; do
    echo "${server}" >> "${CONFIG}"
done

# Update log4j
sed -i "/zookeeper.log.dir=/c\zookeeper.log.dir=${ZOO_LOG_DIR}" ${ZOO_CONF_DIR}/log4j.properties

# Update zkServer enviornemnt
HDF_VERSION=$(hdf-select versions | tail -1 | tr -d ' ')
sed -i "/ZOO_LOG_DIR=/c\ZOO_LOG_DIR=${ZOO_LOG_DIR}" /usr/hdf/${HDF_VERSION}/zookeeper/bin/zkEnv.sh

mkdir -p ${ZOO_PERSISTENCE_DIR} ${ZOO_DATA_DIR}

# Write myid
echo "${ZOO_MY_ID:-1}" > "${ZOO_DATA_DIR}/myid"
current_id=$( cat ${ZOO_DATA_DIR}/myid | tr -d ' ' )
sed -i "/server.${current_id}/c\server.${current_id}=0.0.0.0:2888:3888" ${CONFIG}

# Start Zookeeper
zookeeper-server start-foreground &
zoo_pid=${!}

while ! ls ${ZOO_LOG_DIR} | grep -q 'zookeeper--server'
do
    sleep 1s
done

find ${ZOO_LOG_DIR} -name 'zookeeper--server-*.log' -exec tail -F {} \; &

trap "printf 'Received trapped signal, beginning shutdown\n'...;" KILL TERM HUP INT EXIT;

printf "Zookeeper running with PID %s\n" ${zoo_pid}.
wait ${zoo_pid}
