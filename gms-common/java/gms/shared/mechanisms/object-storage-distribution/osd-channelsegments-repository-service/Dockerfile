ARG DOCKER_REGISTRY
FROM $DOCKER_REGISTRY/gms/centos7/openjdk9:latest

WORKDIR /opt/gms

EXPOSE 8080

#Copying .jar file to /root
ADD osd-channelsegments-repository-service*.tar ./

RUN mv osd-channelsegments-repository-service* osd-channelsegments-repository-service

WORKDIR osd-channelsegments-repository-service

# Define default command.
CMD ["./bin/osd-channelsegments-repository-service"]
