# Docker Build Arguments
ARG FROM_IMAGE
FROM $FROM_IMAGE


RUN setproxy && \
    yum update -y && \
    curl -L "https://github.com/docker/compose/releases/download/1.23.2/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose && \
    chmod +x /usr/local/bin/docker-compose && \
    unsetproxy

CMD ["/bin/bash"]
