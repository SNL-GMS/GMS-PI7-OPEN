# Docker build arguments
ARG DOCKER_REGISTRY
FROM $DOCKER_REGISTRY/gms/centos7/openjdk9:latest

WORKDIR /opt/gms

#Copying .jar file to /root
ADD transferred-file-cleanup-utility*.tar ./

RUN mv transferred-file-cleanup-utility* transferred-file-cleanup-utility

WORKDIR transferred-file-cleanup-utility

# Define default command.
CMD ["./bin/transferred-file-cleanup-utility"]
