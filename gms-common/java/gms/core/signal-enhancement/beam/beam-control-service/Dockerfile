# Sets up Beam Control Service
# sudo docker build -t beam-control-service .

ARG DOCKER_REGISTRY
FROM $DOCKER_REGISTRY/gms/centos7/openjdk9:latest

WORKDIR /opt/gms

EXPOSE 8080

#Copying .jar file to /root
COPY beam-control-service*.tar ./

RUN mkdir beam-control-service && tar -xf beam-control-service*.tar --strip 1 -C beam-control-service && rm -f beam-control-service*.tar

HEALTHCHECK --interval=5s --timeout=10s --retries=10 \
 CMD curl localhost:8080/signal-enhancement/beam/alive || exit 1

WORKDIR beam-control-service

# Define default command.
CMD ["./bin/beam-control-service"]
