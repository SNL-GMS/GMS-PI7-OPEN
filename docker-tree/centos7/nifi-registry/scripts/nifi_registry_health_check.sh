#!/bin/bash

set -x

#
# Description: Validates all clustered NiFi nodes are connected then exit
#

# HTTP or HTTPS
NIFI_REGISTRY_PROTOCOL=$1

# Hostname of NiFi node
NIFI_REGISTRY_HOST=$2

# Nifi WebUI Port
NIFI_REGISTRY_PORT=$3

# Validate parameteres are passed in
if [ -z ${NIFI_REGISTRY_PROTOCOL} ] || [ -z ${NIFI_REGISTRY_HOST} ] || [ -z ${NIFI_REGISTRY_PORT} ]; then
    echo "Usage ./nifi_health_check.sh [NIFI_REGISTRY_PROTOCOL] [NIFI_REGISTRY_HOST] [NIFI_REGISTRY_PORT]"
    exit 1
fi

# Get the InitialHTTP Response from NiFi
response=$(curl -L -k --write-out %{http_code} --silent --output /dev/null "${NIFI_REGISTRY_PROTOCOL}://${NIFI_REGISTRY_HOST}:${NIFI_REGISTRY_PORT}/nifi-registry")

# Wait for NiFi UI to be reachable
if [ $response -ne 200 ]; then
    exit 1
fi

exit 0
