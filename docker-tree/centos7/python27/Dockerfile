# Docker Build Arguments
ARG FROM_IMAGE
FROM $FROM_IMAGE


# Install Python 2.7
RUN setproxy && \
    yum update -y && \
    yum install -y make gcc openssl-devel bzip2-devel && \
    curl -OL https://www.python.org/ftp/python/2.7.14/Python-2.7.14.tgz && \
    tar -xzf Python-2.7.14.tgz && \
    rm Python-2.7.14.tgz && \
    cd Python-2.7.14 && \
    ./configure --enable-optimizations && \
    make install && \
    yum remove -y make && \
    yum clean all && \
    unsetproxy

CMD ["python"]
