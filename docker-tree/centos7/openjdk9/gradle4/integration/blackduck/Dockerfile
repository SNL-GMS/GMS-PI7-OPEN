# Docker Build Arguments
ARG FROM_IMAGE
FROM $FROM_IMAGE


# Declare Black Duck Version
ENV BD_VER 5.3.2
ENV BLACKDUCK_SKIP_PHONE_HOME true

# Install Black Duck
WORKDIR /usr/local

RUN setproxy && \
    yum -y install unzip vim-common make gcc-c++ && \
    curl -OL https://repo.blackducksoftware.com/artifactory/bds-integrations-release/com/synopsys/integration/synopsys-detect/${BD_VER}/synopsys-detect-${BD_VER}-air-gap.zip && \
    unzip /usr/local/synopsys-detect-$BD_VER-air-gap.zip && \
    rm -f /usr/local/synopsys-detect-$BD_VER-air-gap.zip && \
    curl -sL https://rpm.nodesource.com/setup_8.x | bash - && \
    yum install -y nodejs && \
    unsetproxy

CMD ["java", "-jar", "/usr/local/hub-detect-$BD_VER.jar"]
