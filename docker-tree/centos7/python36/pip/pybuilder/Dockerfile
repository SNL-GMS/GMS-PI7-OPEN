# Docker Build Arguments
ARG FROM_IMAGE
FROM $FROM_IMAGE


ARG SONAR_SCANNER_VER=3.3.0.1492

RUN setproxy && \
    curl -OL https://binaries.sonarsource.com/Distribution/sonar-scanner-cli/sonar-scanner-cli-$SONAR_SCANNER_VER-linux.zip && \
    yum install -y python36u-pip && \
    yum install -y gcc && \
    yum install -y unzip && \
    pip3.6 install --upgrade twine && \
    pip3.6 install --upgrade pytest && \
    pip3.6 install --upgrade pipenv && \
    pip3.6 install --upgrade pylint && \
    pip3.6 install --upgrade coverage && \
    yum clean all -y && \
    unsetproxy && \
    unzip sonar-scanner-cli-$SONAR_SCANNER_VER-linux.zip && \
    chown root:root -R sonar-scanner-$SONAR_SCANNER_VER-linux && \
    ln -s $( pwd )/sonar-scanner-$SONAR_SCANNER_VER-linux/bin/sonar-scanner /usr/bin/sonar-scanner && \
    rm -f sonar-scanner-cli-$SONAR_SCANNER_VER-linux.zip

CMD ["/bin/bash"]
