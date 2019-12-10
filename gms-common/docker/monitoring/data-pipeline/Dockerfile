ARG DOCKER_REGISTRY
FROM $DOCKER_REGISTRY/gms/centos7:latest

ARG POSTGRES_URL=localhost
ARG POSTGRES_PORT=5432
ARG POSTGRES_USER=xmp
ARG POSTGRES_PASS=gmsdb:xmp@postgres=bird-hero-calendar
ARG POSTGRES_DB=xmp_metadata
ARG TO_EMAIL
ARG SMTP_SERVER

ENV POSTGRES_URL=${POSTGRES_URL} \
    POSTGRES_PORT=${POSTGRES_PORT} \
    POSTGRES_USER=${POSTGRES_USER} \
    POSTGRES_PASS=${POSTGRES_PASS} \
    POSTGRES_DB=${POSTGRES_DB} \
    TO_EMAIL=${TO_EMAIL} \
    SMTP_SERVER=${SMTP_SERVER}

WORKDIR /etc/mail

COPY docker-entrypoint.sh email-if-not-storing-data.sh gen_mailrc.sh /usr/local/
COPY .mailrc /root/.mailrc

RUN setproxy && \
    yum -y update && \
    yum install -y m4 mailx sendmail sendmail-cf postgresql make && \
    unsetproxy

COPY sendmail.mc /etc/mail/sendmail.mc

RUN m4 sendmail.mc > sendmail.cf && \
    echo "Connect:172 RELAY" >> access && \
    echo "Connect:10 RELAY" >> access && \
    make && \
    yum remove -y m4 make && \
    yum clean all

WORKDIR /usr/local

VOLUME /opt/persistence

ENTRYPOINT ["./docker-entrypoint.sh"]
