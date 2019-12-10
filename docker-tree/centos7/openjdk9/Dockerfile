# Docker Build Arguments
ARG FROM_IMAGE
FROM $FROM_IMAGE


# Address JVM Heap Sizing Issues due to Docker's use of cgroups for containerization
# See: https://developers.redhat.com/blog/2017/03/14/java-inside-docker/ &
#      https://github.com/redhat-developer-demos/java-container/blob/master/Dockerfile.openjdk-cgroup#L5
ENV JAVA_TOOL_OPTIONS="-XX:+UnlockExperimentalVMOptions -XX:+UseCGroupMemoryLimitForHeap"

# Set Java Home
ENV JAVA_VER 9.0.4
ENV JAVA_HOME /opt/jdk-${JAVA_VER}

COPY opt/ /opt/docker/

WORKDIR /usr/local

RUN setproxy && \
    curl -L https://download.java.net/java/GA/jdk9/${JAVA_VER}/binaries/openjdk-${JAVA_VER}_linux-x64_bin.tar.gz \
    -o jdk-$JAVA_VER.tar.gz && \
    unsetproxy && \
    tar -xzf jdk-$JAVA_VER.tar.gz -C /opt && \
    rm jdk-$JAVA_VER.tar.gz && \
    load_opt && \
    alternatives --install "/usr/bin/java" "java" "${JAVA_HOME}/bin/java" 1 && \
    alternatives --install "/usr/bin/javac" "javac" "${JAVA_HOME}/bin/javac" 1 && \
    alternatives --install "/usr/bin/jshell" "jshell"  "${JAVA_HOME}/bin/jshell" 1 && \
    alternatives --set java "${JAVA_HOME}/bin/java" && \
    update-alternatives --set javac "${JAVA_HOME}/bin/javac"

CMD ["jshell"]
