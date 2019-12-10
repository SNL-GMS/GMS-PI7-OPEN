# Docker Build Arguments
ARG FROM_IMAGE
FROM $FROM_IMAGE


ENV GRADLE_VER 4.10
ENV JAVA_TOOL_OPTIONS="-Dhttp.socketTimeout=60000 -Dhttp.connectionTimeout=60000 ${JAVA_TOOL_OPTIONS}"

WORKDIR /usr/local

COPY src/set_hosts.sh /usr/local/set_hosts.sh

RUN setproxy && \
    yum --enablerepo=updates clean metadata && \
    yum update -y && \
    yum install -y unzip && \
    curl -OL https://downloads.gradle.org/distributions/gradle-${GRADLE_VER}-bin.zip && \
    unsetproxy && \
    mkdir /opt/gradle && \
    unzip gradle-${GRADLE_VER}-bin.zip -d /opt/gradle && \
    ln -s /opt/gradle/gradle-${GRADLE_VER}/bin/gradle /usr/bin/gradle && \
    rm -rf /usr/local/gradle-${GRADLE_VER}-bin.zip && \
    yum remove -y unzip && \
    yum clean all && \
    rm -rf /var/cache/yum

ENTRYPOINT ./set_hosts.sh $(echo "${REPOSITORY_URL}" | awk -F/ '{print $3}') && /bin/bash

CMD ["gradle"]
