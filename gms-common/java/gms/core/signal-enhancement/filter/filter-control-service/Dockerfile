ARG DOCKER_REGISTRY
FROM $DOCKER_REGISTRY/gms/centos7/openjdk9:latest

WORKDIR /opt/gms

EXPOSE 8080

#Copying .jar file to /root
ADD filter-control-service*.tar ./

RUN mv filter-control-service* filter-control-service

WORKDIR filter-control-service

HEALTHCHECK --interval=5s --timeout=10s --retries=10 \
 CMD curl -sf localhost:8080/signal-enhancement/waveform-filtering/alive || exit 1

# Define default command.
CMD ["./bin/filter-control-service"]
