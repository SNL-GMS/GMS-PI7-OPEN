#!/bin/bash
find ./docker/opt ! -name 'verify_scripts.sh' -type f -exec rm -f {} +
rm -rf ./docker/opt/src
rm -rf ./node10/opt/*
rm -rf ./node8/opt/*
rm -rf ./centos7/openjdk9/maven3/opt/*
rm -rf ./centos7/openjdk9/opt/*
rm -rf ./centos7/opt/*
rm -rf ./centos7/openjdk11/maven3/opt/*
rm -rf ./centos7/openjdk11/opt/*

rm -rf ./env.sh
echo "export DOCKER_REGISTRY=localhost" >> env.sh
echo "export REGISTRY_BASE=gms" >> env.sh
echo "export TAG=latest" >> env.sh
echo "export PROXY_URL=" >> env.sh
echo "export NO_PROXY_PATHS=" >> env.sh
echo "export REPOSITORY_URL=" >> env.sh
chmod +x ./env.sh
