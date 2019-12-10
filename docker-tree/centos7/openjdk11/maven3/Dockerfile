# Docker Build Arguments
ARG FROM_IMAGE
FROM $FROM_IMAGE


ENV MAVEN_MAJ 3
ENV MAVEN_VERSION 3.5.3
ENV MAVEN_HOME /opt/apache-maven-${MAVEN_VERSION}

ENV PATH ${MAVEN_HOME}/bin:$PATH

COPY opt/ /opt/docker/

WORKDIR /opt

RUN setproxy && \
    curl -OL https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/${MAVEN_VERSION}/apache-maven-${MAVEN_VERSION}-bin.tar.gz && \
    tar -xzf apache-maven-${MAVEN_VERSION}-bin.tar.gz -C /opt && \
    rm apache-maven-${MAVEN_VERSION}-bin.tar.gz && \
    unsetproxy && \
    load_opt

CMD ["mvn"]
