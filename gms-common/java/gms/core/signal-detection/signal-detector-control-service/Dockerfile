ARG DOCKER_REGISTRY
FROM $DOCKER_REGISTRY/gms/centos7/openjdk9:latest

WORKDIR /opt/gms

ARG PORT=8080
ENV PORT=${PORT}
EXPOSE ${PORT}

COPY check_health.sh /opt/gms/

HEALTHCHECK --interval=10s --timeout=10s --retries=5 CMD ["bash", "/opt/gms/check_health.sh"]

#Copying .jar file to /root
ADD signal-detector-control-service-*.tar ./
RUN mv signal-detector-control-service-* signal-detector-control-service
WORKDIR signal-detector-control-service

# Define default command.
CMD ["./bin/signal-detector-control-service"]

