ARG DOCKER_REGISTRY
FROM $DOCKER_REGISTRY/gms/centos7:latest

WORKDIR /frame-management/

ENV HARDLINK_DIRECTORIES=

RUN mkdir shared-volume && chmod -R 777 shared-volume/

COPY delete-old-files.sh hardlink-files.sh docker-entrypoint.sh ./

CMD ["./docker-entrypoint.sh"]
