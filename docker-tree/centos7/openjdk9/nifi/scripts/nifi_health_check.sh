#!/bin/bash

set -x 

#
# Description: Validates all clustered NiFi nodes are connected then exit
#

# HTTP or HTTPS
NIFI_PROTOCOL=$1

# Hostname of NiFi node
NIFI_HOST=$2

# Nifi WebUI Port
NIFI_PORT=$3

# Validate parameteres are passed in
if [ -z ${NIFI_PROTOCOL} ] || [ -z ${NIFI_HOST} ] || [ -z ${NIFI_PORT} ]; then
    echo "Usage ./nifi_health_check.sh [NIFI_PROTOCOL] [NIFI_HOST] [NIFI_PORT]"
    exit 1 
fi

# Get the InitialHTTP Response from NiFi
response=$(curl -k --write-out %{http_code} --silent --output /dev/null "${NIFI_PROTOCOL}://${NIFI_HOST}:${NIFI_PORT}")

# Wait for NiFi UI to be reachable
if [ $response -ne 200 ]; then
    exit 1
fi

# Get the initial NiFi Cluster API return
resp_type=$(curl -sk -X GET "${NIFI_PROTOCOL}://${NIFI_HOST}:${NIFI_PORT}/nifi-api/controller/cluster" | jq type 2> /dev/null)
ret_code=$?
# Wait for NiFI Cluster API to return JSON 
if [ $ret_code -ne 0 ]; then
    exit 1
fi

# Check for Unconnected Nodes
unconnected_node_count=$(curl -sk -X GET "${NIFI_PROTOCOL}://${NIFI_HOST}:${NIFI_PORT}/nifi-api/controller/cluster" | jq ".cluster.nodes[].status" | grep -v 'CONNECTED' | wc -l | tr -d ' ')

# Wait for all nodes to be connected
if [ $unconnected_node_count -ne 0 ]; then
    exit 1
fi

# Finished
exit 0
