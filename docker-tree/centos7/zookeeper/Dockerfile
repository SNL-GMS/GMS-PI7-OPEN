ARG FROM_IMAGE
FROM $FROM_IMAGE


ARG UID=1001
ARG GID=0
ARG ZOO_MY_ID=1
ARG ZOO_SERVERS='server.1=zoo1:2888:3888 server.2=zoo2:2888:3888 server.3=zoo3:2888:3888'
ARG HDF_MAJOR_VER=3
ARG HDF_MINOR_VER=2

# Add enviornemnt variables
ENV ZOO_PERSISTENCE_DIR=/grid 
ENV ZOO_USER=zookeeper \
    ZOO_CONF_DIR=/etc/zookeeper/conf \
    ZOO_DATA_DIR=${ZOO_PERSISTENCE_DIR}/zookeeper/data \
    ZOO_LOG_DIR=/var/log/zookeeper \
    ZOO_RUN_DIR=/run/zookeeper \
    ZOO_PORT=2181 \
    ZOO_TICK_TIME=2000 \
    ZOO_INIT_LIMIT=10 \
    ZOO_SYNC_LIMIT=5 \
    HDF_DIR=/usr/hdf \
    ZOO_SERVERS=${ZOO_SERVERS} \
    JAVA_HOME=/usr/lib/jvm/java-1.8.0-openjdk


RUN setproxy && \
    curl -o /tmp/HDF-3.2.0.0-centos7-rpm.tar.gz -OL http://public-repo-1.hortonworks.com/HDF/centos7/3.x/updates/3.2.0.0/HDF-3.2.0.0-centos7-rpm.tar.gz && \
    tar -xvf /tmp/HDF-3.2.0.0-centos7-rpm.tar.gz -C /tmp && \
    rm /tmp/*.tar.gz && \
    yum -y localinstall /tmp/HDF/centos7/3.2.0.0-520/hdf-select/hdf-select*.rpm /tmp/HDF/centos7/3.2.0.0-520/zookeeper/zookeeper*.rpm && \
    yum -y install java-1.8.0-openjdk java-1.8.0-openjdk-devel nmap-ncat && \
    rm -rf /tmp/HDF && \
    unsetproxy && \
    mkdir -p ${ZOO_LOG_DIR} ${ZOO_CONF_DIR} ${ZOO_RUN_DIR} ${HDF_DIR} ${ZOO_PERSISTENCE_DIR} && \
    chgrp -R 0 ${ZOO_LOG_DIR} ${ZOO_CONF_DIR} ${ZOO_RUN_DIR} ${HDF_DIR} ${ZOO_PERSISTENCE_DIR} && \
    chmod -R g=u ${ZOO_LOG_DIR} ${ZOO_CONF_DIR} ${ZOO_RUN_DIR} ${HDF_DIR} ${ZOO_PERSISTENCE_DIR} && \
    rm -rf /tmp/hdf_repos

VOLUME ["${ZOO_PERSISTENCE_DIR}"]

EXPOSE 2181 2888 3888

COPY scripts /

USER 1001

HEALTHCHECK --interval=5s --timeout=10s --start-period=20s --retries=10 \
    CMD /zookeeper_health_check.sh

ENTRYPOINT ["/start.sh"]
