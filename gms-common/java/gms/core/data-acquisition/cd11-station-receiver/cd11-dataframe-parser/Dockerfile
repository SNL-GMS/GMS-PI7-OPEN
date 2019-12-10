# Sets up Dataframe parser. Monitors the shared volume /cd11-dataframe-parser/monitored-dir for file creation
# sudo docker build -f Dockerfile-Monitor-Parser -t dare-monitor-parser .

ARG DOCKER_REGISTRY
FROM $DOCKER_REGISTRY/gms/centos7/openjdk9:latest

#Expose postgres port
EXPOSE 5432

ARG HTTP_PROXY
ARG HTTPS_PROXY
ARG FTP_PROXY
ARG ALL_PROXY
ARG NO_PROXY

# Proxy Settings
ENV HTTP_PROXY=$HTTP_PROXY http_proxy=$HTTP_PROXY FTP_PROXY=$FTP_PROXY ftp_proxy=$FTP_PROXY all_proxy=$ALL_PROXY ALL_PROXY=$ALL_PROXY https_proxy=$HTTPS_PROXY HTTPS_PROXY=$HTTPS_PROXY NO_PROXY=$NO_PROXY no_proxy=$NO_PROXY

# Copy tars, untar them
WORKDIR /cd11-dataframe-parser/

COPY ./cd11-dataframe-parser-*.tar * ./

RUN mkdir cd11-dataframe-parser && tar -xf cd11-dataframe-parser-*.tar --strip 1 -C cd11-dataframe-parser
RUN rm -f cd11-dataframe-parser-*.tar && mkdir shared-volume && mkdir -p shared-volume/logs && mkdir -p shared-volume/dataframes && chmod -R 777 .

RUN chgrp -R 0 /cd11-dataframe-parser && \
    chmod -R g=u /cd11-dataframe-parser && \
    chmod -R g+s /cd11-dataframe-parser

# Start parser and rsync
CMD cd11-dataframe-parser/bin/cd11-dataframe-parser 
