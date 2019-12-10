ARG DOCKER_REGISTRY
FROM $DOCKER_REGISTRY/gms/centos7:latest

ARG PRIV_SSH_KEY
ARG CI_CURRENT_PROJECT
ARG CI_CURRENT_ENV
ARG RSYNC_USER=gmsuser
ARG RSYNC_REMOTE_HOST
ARG RSYNC_BASE_DIR=/rsync
ARG RSYNC_REMOTE_DIR=/docker/station-gateway-service/shared-volume/frame-transfer
ARG RSYNC_LOCAL_DIR

ENV CI_CURRENT_PROJECT=${CI_CURRENT_PROJECT} \
    CI_CURRENT_ENV=${CI_CURRENT_ENV} \ 
    RSYNC_USER=${RSYNC_USER} \
    RSYNC_REMOTE_HOST=${RSYNC_REMOTE_HOST} \
    RSYNC_REMOTE_DIR=${RSYNC_REMOTE_DIR} \
    RSYNC_LOCAL_DIR=${RSYNC_BASE_DIR}/shared-volume/rsynced-files/ \
    # RSYNC_JUMP_HOSTS can be a space-separated list of hostnames
    # i.e. RSYNC_JUMP_HOSTS="host1 host2 host3"
    RSYNC_JUMP_HOSTS=

WORKDIR /usr/local/bin

COPY rsync-transfer.sh docker-entrypoint.sh ./

RUN useradd -ms /bin/bash ${RSYNC_USER} -G root && \
    mkdir -p ${RSYNC_LOCAL_DIR} && \
    chown -R ${RSYNC_USER}:${RSYNC_USER} ${RSYNC_BASE_DIR}

USER ${RSYNC_USER}

RUN mkdir ~/.ssh && \
    chmod 700 ~/.ssh &&\
    echo "$PRIV_SSH_KEY" > ~/.ssh/id_rsa && \
    chmod 600 ~/.ssh/id_rsa

COPY id_rsa.pub /home/${RSYNC_USER}/.ssh

VOLUME ${RSYNC_BASE_DIR}/shared-volume
    
CMD ["./docker-entrypoint.sh"] 
