ARG FROM_IMAGE
FROM $FROM_IMAGE

ARG UID=1001
ARG GID=0
ARG NIFI_SERVICE_NAME=nifi
ARG NIFI_PORT=8080
ARG NIFI_REGISTER_SLEEP=20
ARG NIFI_REGISTRY_HOST=nifi-registry
ARG NIFI_REGISTRY_HTTP_PORT=18080
ARG HDF_MAJOR_VER=3
ARG HDF_MINOR_VER=2

# Add enviornemnt variables
ENV NIFI_REGISTRY_PERSISTENCE_DIR=/grid \
    HDF_HOME=/usr/hdf
ENV NIFI_REGISTRY_USER=nifiregistry \
    NIFI_REGISTRY_SCRIPTS_DIR=/usr/hdf/current/nifi-registry \
    NIFI_REGISTRY_CONF_DIR=/etc/nifi-registry/conf \
    NIFI_REGISTRY_LOG_DIR=/var/log/nifi-registry \
    NIFI_REGISTRY_RUN_DIR=/var/run/nifi-registry \
    NIFI_REGISTRY_WORK_DIR=/var/lib/nifi-registry \
    NIFI_REGISTRY_DATABASE_REPOSITORY=${NIFI_REGISTRY_PERSISTENCE_DIR}/nifi-registry/database_repository \
    NIFI_REGISTRY_HOST=${NIFI_REGISTRY_HOST} \
    NIFI_REGISTRY_HTTP_PORT=${NIFI_REGISTRY_HTTP_PORT} \
    NIFI_SERVICE_NAME=${NIFI_SERVICE_NAME} \
    NIFI_PORT=${NIFI_PORT} \
    NIFI_REGISTER_SLEEP=${NIFI_REGISTER_SLEEP} \
    JAVA_HOME=/usr/lib/jvm/java-1.8.0-openjdk

# NiFi Registry repos
RUN setproxy && \
    curl -L http://public-repo-1.hortonworks.com/HDF/centos7/3.x/updates/3.2.0.0/HDF-3.2.0.0-centos7-rpm.tar.gz \
    -o /tmp/nifi_rpms_hdf${HDF_MAJOR_VER}-${HDF_MINOR_VER}.tar.gz && \
    tar -xvf /tmp/nifi_rpms_hdf${HDF_MAJOR_VER}-${HDF_MINOR_VER}.tar.gz -C /tmp && \
    rm /tmp/*.tar.gz && \
    mkdir -p ${NIFI_REGISTRY_LOG_DIR} ${NIFI_REGISTRY_RUN_DIR} ${NIFI_REGISTRY_WORK_DIR} ${NIFI_REGISTRY_PERSISTENCE_DIR} ${NIFI_REGISTRY_CONF_DIR} ${HDF_HOME} && \
    yum -y localinstall /tmp/HDF/centos7/3.2.0.0-520/hdf-select/hdf-select*.rpm /tmp/HDF/centos7/3.2.0.0-520/nifi_registry/nifi-registry*.rpm && \
    yum clean all && \
    yum install -y epel-release && \
    rpm --rebuilddb && \
    yum install -y jq \ 
                   curl \
    		   which \
		   tar \
		   openssh-server \
		   openssh-clients \
		   rsync \
		   less \
		   iproute \
		   net-tools \
		   bind-utils \
		   initscripts \
		   java-1.8.0-openjdk \
		   java-1.8.0-openjdk-devel && \
    yum update -y libselinux && \
    unsetproxy && \
    rm -rf /tmp/HDF* && \
    chgrp -R 0 ${NIFI_REGISTRY_LOG_DIR} ${NIFI_REGISTRY_RUN_DIR} ${NIFI_REGISTRY_WORK_DIR} ${NIFI_REGISTRY_CONF_DIR} ${NIFI_REGISTRY_PERSISTENCE_DIR} ${HDF_HOME} && \
    chmod -R g=u ${NIFI_REGISTRY_LOG_DIR} ${NIFI_REGISTRY_RUN_DIR} ${NIFI_REGISTRY_WORK_DIR} ${NIFI_REGISTRY_CONF_DIR} ${NIFI_REGISTRY_PERSISTENCE_DIR} ${HDF_HOME} 


VOLUME ["${NIFI_REGISTRY_PERSISTENCE_DIR}"]

EXPOSE 61080

USER 1001

COPY conf /etc/nifi-registry/conf
COPY scripts ${NIFI_REGISTRY_SCRIPTS_DIR}

# Define the healthcheck
HEALTHCHECK --interval=5s --timeout=10s --start-period=45s --retries=10 \
    CMD /${NIFI_REGISTRY_SCRIPTS_DIR}/nifi_registry_health_check.sh http $(hostname -f) ${NIFI_REGISTRY_HTTP_PORT}

# Workdir scripts
WORKDIR ${NIFI_REGISTRY_SCRIPTS_DIR}

ENTRYPOINT ["./bootstrap.sh"]
