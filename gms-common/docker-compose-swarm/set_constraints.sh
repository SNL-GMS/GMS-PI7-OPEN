#!/bin/bash

if [[ -z "$SUBDOMAIN" ]]; then
  echo \
  "ERROR: Variable 'SUBDOMAIN' must be set for example:

       > export SUBDOMAIN=gms
       "
  exit 1
fi


node_id=`docker node ls | grep Leader | awk '{print $1}'`

docker node update --label-add ${SUBDOMAIN}.cassandra=true ${node_id}
docker node update --label-add ${SUBDOMAIN}.cd11-data-acq=true ${node_id}
docker node update --label-add ${SUBDOMAIN}.nifi-data-acq=true ${node_id}
docker node update --label-add ${SUBDOMAIN}.interactive-analysis-api-gateway=true ${node_id}
docker node update --label-add ${SUBDOMAIN}.nifi-registry=true ${node_id}
docker node update --label-add ${SUBDOMAIN}.zoo1=true ${node_id}
docker node update --label-add ${SUBDOMAIN}.zoo2=true ${node_id}
docker node update --label-add ${SUBDOMAIN}.zoo3=true ${node_id}
docker node update --label-add ${SUBDOMAIN}.postgresql-stationreceiver=true ${node_id}
docker node update --label-add ${SUBDOMAIN}.wiremock=true ${node_id}

