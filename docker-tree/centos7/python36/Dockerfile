# Docker Build Arguments
ARG FROM_IMAGE
FROM $FROM_IMAGE


# Install Python 3.6
RUN setproxy && \
    yum install -y https://centos7.iuscommunity.org/ius-release.rpm && \
    yum update -y && \
    yum install -y python36u python36u-libs python36u-devel && \
    yum clean all && \
    ln -s /usr/bin/python3.6 /usr/local/bin/python && \
    unsetproxy

CMD ["python"]
