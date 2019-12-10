# Docker Build Arguments
ARG FROM_IMAGE
FROM $FROM_IMAGE


# Set Java Home
ENV JAVA_VER 11.0.2
ENV JAVA_HOME /opt/jdk-${JAVA_VER}

COPY opt/ /opt/docker/

WORKDIR /usr/local

RUN setproxy && \
    curl -L https://download.java.net/java/GA/jdk11/9/GPL/openjdk-${JAVA_VER}_linux-x64_bin.tar.gz \
    -o jdk-$JAVA_VER.tar.gz && \
    tar -xzf jdk-$JAVA_VER.tar.gz -C /opt && \
    unsetproxy && \
    rm jdk-$JAVA_VER.tar.gz && \
    load_opt && \
    alternatives --install "/usr/bin/java" "java" "${JAVA_HOME}/bin/java" 1 && \
    alternatives --install "/usr/bin/javac" "javac" "${JAVA_HOME}/bin/javac" 1 && \
    alternatives --install "/usr/bin/jshell" "jshell"  "${JAVA_HOME}/bin/jshell" 1 && \
    alternatives --set java "${JAVA_HOME}/bin/java" && \
    update-alternatives --set javac "${JAVA_HOME}/bin/javac"

CMD ["jshell"]
