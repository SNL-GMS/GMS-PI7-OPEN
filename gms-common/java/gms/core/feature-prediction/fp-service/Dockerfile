ARG DOCKER_REGISTRY
FROM $DOCKER_REGISTRY/gms/centos7/openjdk9:latest

WORKDIR /opt/gms

EXPOSE 8080

#Copying .jar file to /root
ADD fp-service*.tar ./

RUN mv fp-service* fp-service

WORKDIR fp-service

# Define default command.
CMD ["./bin/fp-service"]
