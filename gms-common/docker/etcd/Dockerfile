#
#  Dockerfile to make a configured etcd image for GMS.
#
ARG DOCKER_REGISTRY
FROM $DOCKER_REGISTRY/gms/centos7/python36/pip:latest as etcd

# Install etcd
RUN setproxy && \
    yum -y install etcd && \
    unsetproxy

# Configure a single-node etcd server.  ETCD_NAME is definied
# in a separate ENV statement so that it can be interpolated
# later when setting ETCD_INITIAL_CLUSTER.  USER_NAME is used
# by the docker-entrypoint.sh script when naming the user
# inside the container.
ENV ETCD_NAME=etcd1
ENV USER_NAME=etcd \
    ETCDCTL_API=3 \
    ETCD_DATA_DIR=/var/lib/etcd \
    ETCD_INITIAL_ADVERTISE_PEER_URLS=http://127.0.0.1:2380 \
    ETCD_LISTEN_PEER_URLS=http://0.0.0.0:2380 \
    ETCD_ADVERTISE_CLIENT_URLS=http://127.0.0.1:2379 \
    ETCD_LISTEN_CLIENT_URLS=http://0.0.0.0:2379 \
    ETCD_INITIAL_CLUSTER="${ETCD_NAME}=http://127.0.0.1:2380"

# Setup group-write permissions on data directory and /etc/passwd
RUN mkdir -p ${ETCD_DATA_DIR} && \
    chgrp -R 0 ${ETCD_DATA_DIR} /etc/passwd && \
    chmod -R g=u ${ETCD_DATA_DIR} /etc/passwd
WORKDIR /

# Install gms-config Python interface
COPY docker-build/gms-config /setup/gms-config
RUN setproxy && \
    pip install /setup/gms-config \
    && unsetproxy

# Copy in the the system configuration files
COPY docker-build/system/* /setup/config/system/

COPY docker-entrypoint.sh /usr/local/bin/docker-entrypoint.sh
ENTRYPOINT ["/usr/local/bin/docker-entrypoint.sh"]
CMD ["etcd"]

