# Docker build arguments
ARG DOCKER_REGISTRY
FROM $DOCKER_REGISTRY/gms/centos7/openjdk9:latest

WORKDIR /opt/gms

EXPOSE 8080

#Copying .jar file to /root
ADD event-location-control-service*.tar ./

RUN mv event-location-control-service* event-location-control-service

WORKDIR event-location-control-service

# Define default command.
CMD ["./bin/event-location-control-service"]
