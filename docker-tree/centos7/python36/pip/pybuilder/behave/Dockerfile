# Docker Build Arguments
ARG FROM_IMAGE
FROM $FROM_IMAGE


RUN setproxy && \
    yum update -y && \
    yum install docker -y && \
    curl -L https://github.com/docker/compose/releases/download/1.21.2/docker-compose-$(uname -s)-$(uname -m) -o /usr/local/bin/docker-compose && \
    chmod +x /usr/local/bin/docker-compose && \
    yum clean all && \
    pip3 install behave pyyaml && \
    rm -rf /var/cache/yum

CMD ["docker-compose"]
