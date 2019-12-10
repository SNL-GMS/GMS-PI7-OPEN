ARG DOCKER_REGISTRY
FROM $DOCKER_REGISTRY/gms/centos7/python36/pip/pybuilder:latest

ENV PYTHONIOENCODING=utf-8

WORKDIR /usr/local/share

COPY . /usr/local/share

RUN setproxy && \
    python3.6 setup.py install && \
    rm -rf sampleFilesAndHelp callDirectAccessExample.py && \
    rm -rf /var/cache/yum

ENTRYPOINT ["python3.6", "coidataclient.py"]
