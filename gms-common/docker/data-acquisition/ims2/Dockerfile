# Build stage for proxychains4
ARG DOCKER_REGISTRY
FROM $DOCKER_REGISTRY/gms/centos7:latest

RUN setproxy && \
    yum update -y && \
    yum install -y make gcc-c++ && \
    git clone https://github.com/haad/proxychains.git && \
    unsetproxy && \
    cd proxychains && \
    ./configure --enable-static --disable-shared && \
    make


# Build stage for the IMS2.0 Service
ARG DOCKER_REGISTRY
FROM $DOCKER_REGISTRY/gms/centos7/python27/pip:latest

ARG CTBTO_USER
ARG CTBTO_PASS

WORKDIR /ims2

COPY flask_files/requirements.txt /tmp
COPY proxy/proxy.conf /usr/local/share
COPY bin/entrypoint.sh /usr/local/share
# Copying from the first image here:
COPY --from=0 /usr/local/share/proxychains/libproxychains4.so /usr/lib64
COPY --from=0 /usr/local/share/proxychains/proxychains4 /usr/local/bin
COPY nms_client/ nms_client/
COPY test-data/ test-data/
COPY flask_files/ flask_files/

RUN setproxy && \
    pip install -r /tmp/requirements.txt && \
    rm /tmp/requirements.txt && \
    unsetproxy && \
    mkdir /root/.nms_client && \
    echo ${CTBTO_USER}:${CTBTO_PASS} > /root/.nms_client/nms_cred && \
    chmod ugo+x /ims2/nms_client/bin/nms_client.sh

# Hard code environment variables (instead of using something like a .env) since
# these are specific to how the the Docker image is built.
ENV NMS_CLI_HOME="/ims2/nms_client" \
    NMS_USER_DIR="/root/.nms_client" \
    CONF_FILE="/ims2/nms_client/conf/nms_client.conf" \
    PYTHONBIN="/usr/local/bin/python" \
    PYTHONPATH="/ims2/nms_client/lib:/ims2/nms_client/lib/NMSClient-1.3-py2.6.egg:/ims2/nms_client/lib/NMSCommon-3.1rc3-py2.6.egg:/ims2/nms_client/lib/NMSConfiguration-3.1rc3-py2.6.egg:/ims2/nms_client/lib/progressbar.py:/ims2/nms_client/lib/progressbar.pyc" \
    FLASK_APP="/ims2/flask_files/nms_flask_server.py" \
    FLASK_ENV="development" \
    TEST_DATA_DIR="/ims2/test-data"

ENTRYPOINT ["/usr/local/share/entrypoint.sh"]
