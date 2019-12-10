# Docker Build Arguments
ARG FROM_IMAGE=node:10
FROM $FROM_IMAGE

ARG REPOSITORY_URL
ARG NPM_REGISTRY
ARG CI_AUTH_BASE64
ARG CI_EMAIL
ARG ENV_PROXY
ARG ENV_NO_PROXY
ARG ID_RSA_PUB
ARG ID_RSA_PRIV

ENV DEBIAN_FRONTEND="noninteractive" \
    REPOSITORY_URL=${REPOSITORY_URL} \
    HOME_DIR=/usr/local/share \
    ENV=/etc/profile \
    ENV_PROXY=${ENV_PROXY} \
    ENV_NO_PROXY=${ENV_NO_PROXY} \
    HTTP_PROXY= \
    http_proxy= \
    HTTPS_PROXY= \
    https_proxy= \
    no_proxy= \
    NO_PROXY=

COPY src/proxy_toggle.sh src/load_opt.sh /etc/profile.d/
COPY opt/ /opt/docker/

RUN cat /etc/profile.d/proxy_toggle.sh >> /etc/bash.bashrc && \
    sed -i 's/mesg n/test \-t 0 \&\& mesg n/g' /root/.profile && \
    mkdir -p ~/.ssh && \
    chmod 700 ~/.ssh && \
    echo "$ID_RSA_PUB" > ~/.ssh/id_rsa.pub && \
    echo "$ID_RSA_PRIV" > ~/.ssh/id_rsa && \
    chmod 600 ~/.ssh/id_rsa

SHELL ["/bin/sh", "-lc"]

# NOTE to generate CI_AUTH_BASE64, use
# echo -n '<username>:<encryptedpassword>' | base64

RUN setproxy && \
    apt-get update && \
    apt-get install apt-utils ca-certificates curl git -y --no-install-recommends && \
    npm config set cafile /etc/ssl/certs/ca-certificates.crt && \
    npm config set spin false && \
    npm config set loglevel info && \
    npm config set registry $NPM_REGISTRY && \
    npm config set _auth $CI_AUTH_BASE64 && \
    npm config set email $CI_EMAIL && \
    npm config set always-auth true && \
    apt-get autoclean && \
    apt-get --purge -y autoremove && \
    rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*

CMD ["node"]
