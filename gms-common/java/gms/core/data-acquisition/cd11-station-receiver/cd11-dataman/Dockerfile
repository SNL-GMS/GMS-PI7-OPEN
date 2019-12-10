# Docker build arguments
ARG DOCKER_REGISTRY
FROM $DOCKER_REGISTRY/gms/centos7/openjdk9:latest

ARG HTTP_PROXY
ARG HTTPS_PROXY
ARG FTP_PROXY
ARG ALL_PROXY
ARG NO_PROXY

# Proxy Settings
ENV HTTP_PROXY=$HTTP_PROXY http_proxy=$HTTP_PROXY FTP_PROXY=$FTP_PROXY ftp_proxy=$FTP_PROXY all_proxy=$ALL_PROXY ALL_PROXY=$ALL_PROXY https_proxy=$HTTPS_PROXY HTTPS_PROXY=$HTTPS_PROXY NO_PROXY=$NO_PROXY no_proxy=$NO_PROXY

# Copy tars, untar them
WORKDIR /dare-receiver/

COPY cd11-dataman-*.tar ./wait-for-it.sh ./

RUN mkdir cd11-dataman && mkdir -p shared-volume/logs/stations && mkdir -p shared-volume/dataframes && chmod -R 777 shared-volume/ && \
    tar -xf cd11-dataman-*.tar -C cd11-dataman --strip-components 1 && rm -f cd11-dataman-*.tar && mkdir -p shared-volume/logs/stations \
    && mkdir -p shared-volume/dataframes && chmod -R 777 shared-volume/ && chmod 777 wait-for-it.sh

# Start cd11-dataman
CMD ["./wait-for-it.sh" , "stationreference-coi-service:8080", "--", "./wait-for-it.sh" , "waveforms-repository-service:8080", "--", "cd11-dataman/bin/cd11-dataman"]
