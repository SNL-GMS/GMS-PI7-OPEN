#!/usr/bin/env bash

docker rm signal-detection-association-control-service

cd ../
gradle docker
cd ./integration
