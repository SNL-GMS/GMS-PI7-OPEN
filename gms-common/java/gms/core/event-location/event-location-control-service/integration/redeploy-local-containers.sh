#!/usr/bin/env bash

docker-compose -f docker-compose_local.yml stop event-location-control-service osd-signaldetection-repository-service osd-stationreference-coi-service

bash rebuild-local-containers.sh

docker-compose -f docker-compose_local.yml up -d event-location-control-service osd-signaldetection-repository-service osd-stationreference-coi-service