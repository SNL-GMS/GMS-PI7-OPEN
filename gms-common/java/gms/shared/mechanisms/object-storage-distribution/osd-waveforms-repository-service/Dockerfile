ARG DOCKER_REGISTRY
FROM $DOCKER_REGISTRY/gms/centos7/openjdk9:latest

WORKDIR /opt/gms

EXPOSE 8080

#Copying jar to docker container
ADD ./osd-waveforms-repository-service*.tar ./
RUN mv osd-waveforms-repository-service* osd-waveforms-repository-service

HEALTHCHECK --interval=10s --timeout=10s --retries=10 \
  CMD curl -fm 10 localhost:8080/mechanisms/object-storage-distribution/waveforms/alive \
    || exit 1

WORKDIR osd-waveforms-repository-service

#Running the jar in the docker container
CMD ["./bin/osd-waveforms-repository-service"]
