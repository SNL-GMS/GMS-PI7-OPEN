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
WORKDIR /receiver/

COPY seedlink-receiver-*.tar ./

RUN mkdir seedlink-receiver

RUN tar -xf seedlink-receiver-*.tar -C seedlink-receiver --strip-components 1

CMD seedlink-receiver/bin/seedlink-receiver
