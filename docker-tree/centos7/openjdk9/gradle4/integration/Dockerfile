# Docker Build Arguments
ARG FROM_IMAGE
FROM $FROM_IMAGE


COPY src/requirements.txt $WORKDIR
COPY src/gms-configuration /usr/local/bin

RUN setproxy && \
    yum update -y && \
    yum groupinstall -y development && \
    yum -y install https://centos7.iuscommunity.org/ius-release.rpm && \
    curl -sL https://rpm.nodesource.com/setup_10.x | bash - && \
    yum -y install python36u python36u-pip nodejs && \
    ln -s /usr/bin/python3.6 /usr/bin/python3 && \
    python3 -m pip install --upgrade pip && \
    python3 -m pip install Jinja2 && \
    yum install -y yum-utils device-mapper-persistent-data lvm2 && \
    yum-config-manager --add-repo https://download.docker.com/linux/centos/docker-ce.repo && \
    yum install -y docker-ce && \
    curl -L https://github.com/docker/compose/releases/download/1.21.2/docker-compose-$(uname -s)-$(uname -m) -o /usr/local/bin/docker-compose && \
    chmod +x /usr/local/bin/docker-compose && \
    python3 -m pip install -r requirements.txt && \
    unsetproxy && \
    yum clean all && \
    rm -rf /var/cache/yum

CMD ["docker-compose"]
