ARG DOCKER_REGISTRY
FROM $DOCKER_REGISTRY/gms/centos7/openjdk9:latest

WORKDIR /opt/gms

EXPOSE 8080


#Copying .jar file to /root
ADD waveform-qc-control-service-*.tar ./
RUN mv waveform-qc-control-service-* waveform-qc-control-service
WORKDIR waveform-qc-control-service

HEALTHCHECK --interval=5s --timeout=10s --retries=10 \
 CMD curl -sfm 30 localhost:8080/alive || exit 1

# Define default command.
CMD ["./bin/waveform-qc-control-service"]
