#!/bin/bash

# This script is run as the entrypoint to the etcd docker container.
# It will be run with a single argument, 'etcd'

set -eu

CONFIGURATION_COMPLETE_FILE=${ETCD_DATA_DIR}/.gms-etcd-configuration-complete

#-- Perform initial startup configuration
if [ "$1" = "etcd" -a ! -f "${CONFIGURATION_COMPLETE_FILE}" ]; then

    if [ -z "${GMS_ETCD_ROOT_PASSWORD}" -o -z "${GMS_ETCD_ADMIN_PASSWORD}" -o -z "${GMS_ETCD_PASSWORD}" ]; then
        echo Unable to apply GMS etcd configuration - environment variables must be defined: GMS_ETCD_ROOT_PASSWORD, GMS_ETCD_ADMIN_PASSWORD, GMS_ETCD_PASSWORD
        exit 1
    fi

    #-- Start etcd temporarily for configuration and loading
    etcd &
    etcdpid=$!
    sleep 2
    
    #-- Setup 'read-everything' and 'readwrite-everything' roles
    etcdctl role add read-everything
    etcdctl role add readwrite-everything
    etcdctl role grant-permission --prefix read-everything read ''
    etcdctl role grant-permission --prefix readwrite-everything readwrite ''
    etcdctl user add "gms:${GMS_ETCD_PASSWORD}"
    etcdctl user add "gmsadmin:${GMS_ETCD_ADMIN_PASSWORD}"
    etcdctl user grant-role gms read-everything
    etcdctl user grant-role gmsadmin readwrite-everything
    
    #-- Add 'root' user and enable authentication
    etcdctl user add "root:${GMS_ETCD_ROOT_PASSWORD}"
    etcdctl auth enable
    gms-config --username gmsadmin --password "${GMS_ETCD_ADMIN_PASSWORD}" --endpoints localhost load /setup/config/system/gms-system-configuration.properties

    sleep 2
    
    #-- Stop the now-configured etcd
    kill ${etcdpid}
    
    touch ${CONFIGURATION_COMPLETE_FILE}
fi

#-- Switch control to the etcd process
exec "$@"

