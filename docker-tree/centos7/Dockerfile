# Docker Build Arguments
ARG FROM_IMAGE=centos:7
FROM $FROM_IMAGE


ARG REPOSITORY_URL
ARG ENV_PROXY
ARG ENV_NO_PROXY
ARG ID_RSA_PUB
ARG ID_RSA_PRIV

# ENV is set to run /etc/profile when running /bin/sh
ENV ENV=/etc/profile

ENV REPOSITORY_URL=${REPOSITORY_URL} \
    HOME_DIR=/usr/local/share \
    ENV_PROXY=${ENV_PROXY} \
    ENV_NO_PROXY=${ENV_NO_PROXY} \
    HTTP_PROXY= \
    http_proxy= \
    HTTPS_PROXY= \
    https_proxy= \
    no_proxy= \
    NO_PROXY=

SHELL ["/bin/sh", "-lc"]

WORKDIR ${HOME_DIR}

COPY src/proxy_toggle.sh src/url_encoding.sh src/load_opt.sh /etc/profile.d/
COPY opt/ /opt/docker/

RUN setproxy && \
    mkdir -p ~/.ssh && \
    chmod 700 ~/.ssh && \
    echo "$ID_RSA_PUB" > ~/.ssh/id_rsa.pub && \
    echo "$ID_RSA_PRIV" > ~/.ssh/id_rsa && \
    chmod 600 ~/.ssh/id_rsa && \
    echo "ip_resolv=4" >> /etc/yum.conf && \
    yum update -y && \
    yum install -y git && \
    yum clean all && \
    load_opt && \
    rm -rf /opt/docker && \
    unsetproxy

CMD ["/bin/bash"]
