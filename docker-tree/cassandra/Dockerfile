#
# Dockerfile to make a configured Cassandra image for GMS.
#

# Cassandra 3.11.2 was the latest stable release as of 5/14/2018.
# Check https://hub.docker.com/_/cassandra/ for new versions.
# Docker Build Arguments
ARG FROM_IMAGE=cassandra:3.11.2
FROM $FROM_IMAGE

ARG ENV_PROXY
ARG ENV_NO_PROXY

ENV ENV_PROXY=${ENV_PROXY} \
    ENV_NO_PROXY=${ENV_NO_PROXY} \
    HTTP_PROXY= \
    http_proxy= \
    HTTPS_PROXY= \
    https_proxy= \
    no_proxy= \
    NO_PROXY=

# ENV is set to run /etc/profile when running /bin/sh
ENV ENV=/etc/profile

COPY src/proxy_toggle.sh /etc/profile.d/

SHELL ["/bin/sh", "-lc"]

# Install vim so we have an editor for debugging.
RUN setproxy && \
    apt-get update && \
    apt-get install -y vim && \
    apt-get clean && \
    unsetproxy

# Copy in our modified Cassandra configuration file.
COPY cassandra.yaml /etc/cassandra/cassandra.yaml

# Copy in our custom daemon confs
COPY cassandra.conf /etc/security/limits.d/cassandra.conf

# Create a directory where *.sh scripts can be stored that will automatically
# be executed when Cassandra starts.  Pre-load this directory with
# docker-entrypoint-initdb.d/* from this repository to start, but also make it
# a VOLUME for later overrides.
RUN mkdir /docker-entrypoint-initdb.d
COPY docker-entrypoint-initdb.d/* docker-entrypoint-initdb.d/
#VOLUME /docker-entrypoint-initdb.d

# Wrap the normal entrypoint with a script to help configure
# Cassandra on first startup.
COPY docker-entrypoint-wrapper.sh /docker-entrypoint-wrapper.sh
ENTRYPOINT ["/docker-entrypoint-wrapper.sh"]
CMD ["cassandra", "-f", "-R"]

# OpenShift compatibility.
RUN mkdir -p     /home/cassandra /.cassandra /var/log/cassandra && \
    chgrp -R 0   /home/cassandra /.cassandra /var/log/cassandra && \
    chmod -R g=u /home/cassandra /.cassandra /var/log/cassandra && \
    chmod -R g+s /home/cassandra /.cassandra /var/log/cassandra 
USER cassandra:cassandra
WORKDIR /home/cassandra
