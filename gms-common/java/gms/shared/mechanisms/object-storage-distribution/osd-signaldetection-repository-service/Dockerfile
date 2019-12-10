ARG DOCKER_REGISTRY
FROM $DOCKER_REGISTRY/gms/centos7/openjdk9:latest

WORKDIR /opt/gms

EXPOSE 8080

#Copying jar to docker container
ADD ./osd-signaldetection-repository-service*.tar ./
RUN mv osd-signaldetection-repository-service* osd-signaldetection-repository-service

HEALTHCHECK --interval=5s --timeout=10s --retries=10 \
  CMD curl -f localhost:8080/mechanisms/object-storage-distribution/signal-detection/alive \
    || exit 1

WORKDIR osd-signaldetection-repository-service

#Running the jar in the docker container
CMD ["./bin/osd-signaldetection-repository-service"]
