# Docker Build Arguments
ARG FROM_IMAGE=docker:latest
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

COPY src/load_opt.sh src/proxy_toggle.sh /etc/profile.d/
COPY opt/ /opt/docker/

RUN mkdir -p ~/.ssh && \
    chmod 700 ~/.ssh && \
    echo "$ID_RSA_PUB" > ~/.ssh/id_rsa.pub && \
    echo "$ID_RSA_PRIV" > ~/.ssh/id_rsa && \
    chmod 600 ~/.ssh/id_rsa && \
    setproxy && \
    apk add --no-cache ca-certificates git bash && \
    unsetproxy && \
    load_opt

CMD ["/bin/sh"]
