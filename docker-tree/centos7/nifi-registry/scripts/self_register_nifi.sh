#!/bin/bash

set -x 

#
# Description: Validates all clustered NiFi nodes are connected then registers self
#     as a nifi registry
#

# HTTP or HTTPS
NIFI_PROTOCOL=$1

# Hostname of NiFi node
NIFI_HOST=$2

# Nifi WebUI Port
NIFI_PORT=$3

# Sleep time between curl commands
SLEEP_TIME=$4

# Validate parameteres are passed in
if [ -z ${NIFI_PROTOCOL} ] || [ -z ${NIFI_HOST} ] || [ -z ${NIFI_PORT} ] || [ -z ${SLEEP_TIME} ]; then
    echo "Usage ./self_register_nifi.sh [NIFI_PROTOCOL] [NIFI_HOST] [NIFI_PORT] [SLEEP_TIME]"
    exit 1 
fi

# Get the InitialHTTP Response from NiFi
response=$(curl -k --write-out %{http_code} --silent --output /dev/null "${NIFI_PROTOCOL}://${NIFI_HOST}:${NIFI_PORT}")

# Wait for NiFi UI to be reachable
while [ $response -ne 200 ]; do
    response=$(curl -k --write-out %{http_code} --silent --output /dev/null "${NIFI_PROTOCOL}://${NIFI_HOST}:${NIFI_PORT}")

    if [ $response -ne 200 ]; then
        sleep ${SLEEP_TIME}
    fi
done

# Get the initial NiFi Cluster API return
resp_type=$(curl -sk -X GET "${NIFI_PROTOCOL}://${NIFI_HOST}:${NIFI_PORT}/nifi-api/controller/cluster" | jq type 2> /dev/null)
ret_code=$?
# Wait for NiFI Cluster API to return JSON 
while [ $ret_code -ne 0 ]; do
    resp_type=$(curl -sk -X GET "${NIFI_PROTOCOL}://${NIFI_HOST}:${NIFI_PORT}/nifi-api/controller/cluster" | jq type 2> /dev/null)
    ret_code=$?

    if [ $ret_code -ne 0 ]; then
        sleep ${SLEEP_TIME}
    fi
done

# Check for Unconnected Nodes
unconnected_node_count=$(curl -sk -X GET "${NIFI_PROTOCOL}://${NIFI_HOST}:${NIFI_PORT}/nifi-api/controller/cluster" | tee response.txt | jq ".cluster.nodes[].status" | grep -v 'CONNECTED'| tee grep.txt | wc -l | tr -d ' ')

# Wait for all nodes to be connected
while [ $unconnected_node_count -ne 0 ]; do
    unconnected_node_count=$(curl -sk -X GET "${NIFI_PROTOCOL}://${NIFI_HOST}:${NIFI_PORT}/nifi-api/controller/cluster" | jq ".cluster.nodes[].status" | grep -v 'CONNECTED'| wc -l | tr -d ' ')
    
    if [ $unconnected_node_count -ne 0 ]; then
        sleep ${SLEEP_TIME}
    fi
done

# Create Nifi Registry client
${NIFI_REGISTRY_SCRIPTS_DIR}/create_registry_client.sh

# Finished
exit 0
