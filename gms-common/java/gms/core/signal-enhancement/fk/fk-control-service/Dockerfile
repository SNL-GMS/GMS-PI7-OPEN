ARG DOCKER_REGISTRY
FROM $DOCKER_REGISTRY/gms/centos7/openjdk9:latest

WORKDIR /opt/gms

EXPOSE 8080

#Copying .jar file to /root
ADD fk-control-service*.tar ./

RUN mv fk-control-service* fk-control-service

WORKDIR fk-control-service

HEALTHCHECK --interval=5s --timeout=10s --retries=10 \
 CMD curl -sf localhost:8080/signal-enhancement/fk/alive || exit 1

# Define default command.
CMD ["./bin/fk-control-service"]
