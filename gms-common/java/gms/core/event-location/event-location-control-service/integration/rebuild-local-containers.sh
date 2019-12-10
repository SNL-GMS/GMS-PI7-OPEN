#!/usr/bin/env bash

docker rm event-location-control-service osd-signaldetection-repository-service

cd ../
gradle docker
cd ./integration

cd ../../../../shared/mechanisms/object-storage-distribution/osd-signaldetection-repository-service/
gradle docker
cd ../../../../core/event-location/event-location-control-service/integration

cd ../../../../shared/mechanisms/object-storage-distribution/osd-stationreference-coi-service/
gradle docker
cd ../../../../core/event-location/event-location-control-service/integration
