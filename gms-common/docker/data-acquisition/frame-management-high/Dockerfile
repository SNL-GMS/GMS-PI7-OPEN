ARG DOCKER_REGISTRY
FROM $DOCKER_REGISTRY/gms/centos7:latest

ARG MV_FROM=/shared-volume/rsynced-files/
ARG MV_TO=/shared-volume/dataframes/

WORKDIR /frame-management/

ENV MV_FROM=${MV_FROM} \
    MV_TO=${MV_TO}

COPY delete-old-files.sh move-files.sh docker-entrypoint.sh ./

CMD ["./docker-entrypoint.sh"]
