#!/usr/bin/env bash

docker-compose -f docker-compose_local.yml stop signal-detection-association-control-service

bash rebuild-local-containers.sh
docker-compose -f docker-compose_local.yml up -d signal-detection-association-control-service