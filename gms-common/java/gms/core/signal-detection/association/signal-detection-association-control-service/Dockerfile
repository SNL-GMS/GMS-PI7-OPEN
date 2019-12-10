ARG DOCKER_REGISTRY
FROM $DOCKER_REGISTRY/gms/centos7/openjdk9:latest

WORKDIR /opt/gms

EXPOSE 8080

#Copying .jar file to /root
ADD signal-detection-association-control-service*.tar ./

RUN mv signal-detection-association-control-service* signal-detection-association-control-service

WORKDIR signal-detection-association-control-service

# Define default command.
CMD ["./bin/signal-detection-association-control-service"]
