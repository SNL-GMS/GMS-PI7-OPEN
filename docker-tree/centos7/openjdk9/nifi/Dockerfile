ARG FROM_IMAGE
FROM $FROM_IMAGE


# Configurable arguments
ARG UID=1001
ARG GID=0
ARG NIFI_VERSION=1.7.0
ARG ZOO_QUORUM=zoo1:2181,zoo2:2181,zoo3:2181
ARG HDF_MAJOR_VER=3
ARG HDF_MINOR_VER=2

ENV HDF_HOME=/usr/hdf \
    NIFI_HOME=/usr/hdf/current/nifi 
ENV NIFI_SCRIPTS_DIR=${NIFI_HOME}/scripts \
    NIFI_CONF_DIR=/etc/nifi/conf \
    NIFI_LOG_DIR=/var/log/nifi \
    NIFI_RUN_DIR=/var/run/nifi \
    NIFI_PERSISTENCE_DIR=/grid/persistence \
    NIFI_CLUSTER_PROTOCOL_PORT=8090 \
    ZOO_QUORUM=${ZOO_QUORUM}

# Install NiFi and apply OpenShift compatability hacks
RUN setproxy && \
    curl -L http://public-repo-1.hortonworks.com/HDF/centos7/3.x/updates/3.2.0.0/HDF-3.2.0.0-centos7-rpm.tar.gz \
    -o /tmp/nifi_rpms_hdf${HDF_MAJOR_VER}-${HDF_MINOR_VER}.tar.gz && \
    tar -xvf /tmp/nifi_rpms_hdf${HDF_MAJOR_VER}-${HDF_MINOR_VER}.tar.gz -C /tmp && \
    rm /tmp/*.tar.gz && \
    yum install -y epel-release && \
    yum install -y jq && \
    yum -y localinstall /tmp/HDF/centos7/3.2.0.0-520/hdf-select/hdf-select*.rpm /tmp/HDF/centos7/3.2.0.0-520/nifi/nifi*.rpm && \
    rm -rf /tmp/HDF* && \
    unsetproxy

# Copy over NiFi initialization scripts
COPY scripts ${NIFI_SCRIPTS_DIR}

RUN mkdir -p ${NIFI_PERSISTENCE_DIR} && \
    mkdir -p ${NIFI_HOME}/scripts && \
    mkdir -p ${NIFI_PERSISTENCE_DIR}/gms_processors && \
    chown -R nifi ${NIFI_CONF_DIR} ${NIFI_LOG_DIR} ${NIFI_RUN_DIR} ${HDF_HOME} ${NIFI_PERSISTENCE_DIR} && \
    chgrp -R 0 ${NIFI_CONF_DIR} ${NIFI_LOG_DIR} ${NIFI_RUN_DIR} ${HDF_HOME} ${NIFI_PERSISTENCE_DIR} && \
    chmod -R g=u ${NIFI_CONF_DIR} ${NIFI_LOG_DIR} ${NIFI_RUN_DIR} ${HDF_HOME} ${NIFI_PERSISTENCE_DIR}

# Persistence should be volume mounted
VOLUME ${NIFI_PERSISTENCE_DIR}

# Run as the nifi user
USER 1001

# Define the healthcheck
HEALTHCHECK --interval=20s --timeout=10s --start-period=90s --retries=10 \
    CMD /${NIFI_SCRIPTS_DIR}/nifi_health_check.sh http $(hostname -f) 8080

# Web HTTP Port & Remote Site-to-Site Ports
EXPOSE 8080 8081 8443 ${NIFI_CLUSTER_PROTOCOL_PORT}

# Workdir NIFI_HOME
WORKDIR ${NIFI_SCRIPTS_DIR}

# Startup NiFi
ENTRYPOINT ["./start.sh"]
