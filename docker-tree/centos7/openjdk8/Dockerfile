# Docker Build Arguments
ARG FROM_IMAGE
FROM $FROM_IMAGE


# Declare Java Version
env JAVA_VER 1.8.0

# Install OpenJDK JRE 8
RUN setproxy && \
    yum update -y && \
    yum install -y java-$JAVA_VER-openjdk.x86_64 && \
    yum clean all && \
    unsetproxy

CMD ["java"]
