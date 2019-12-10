ARG DOCKER_REGISTRY
FROM $DOCKER_REGISTRY/gms/centos7/openjdk9:latest

WORKDIR /opt/gms

EXPOSE 8080

#Copying jar to docker container
ADD ./osd-stationreference-coi-service*.tar ./
RUN mv osd-stationreference-coi-service* osd-stationreference-coi-service

HEALTHCHECK --interval=5s --timeout=10s --retries=10 \
  CMD curl -f localhost:8080/mechanisms/object-storage-distribution/station-reference/alive \
    || exit 1

WORKDIR osd-stationreference-coi-service

#Running the jar in the docker container
CMD ["./bin/osd-stationreference-coi-service"]
