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

COPY cd11-connman-*.tar ./

RUN mkdir cd11-connman && mkdir -p shared-volume/logs && chmod -R 777 shared-volume/ \
    && tar -xf cd11-connman-*.tar -C cd11-connman --strip-components 1 && rm -f cd11-connman-*.tar \
    && mkdir -p shared-volume/logs && chmod -R 777 shared-volume/

CMD cd11-connman/bin/cd11-connman
