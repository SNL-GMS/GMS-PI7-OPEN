ARG DOCKER_REGISTRY
FROM $DOCKER_REGISTRY/gms/centos7/openjdk9:latest

WORKDIR /opt/gms

EXPOSE 8080

#Copying .jar file to /root
ADD osd-transferredfile-repository-service*.tar ./

RUN mv osd-transferredfile-repository-service* osd-transferredfile-repository-service

WORKDIR osd-transferredfile-repository-service

# Define default command.
CMD ["./bin/osd-transferredfile-repository-service"]
