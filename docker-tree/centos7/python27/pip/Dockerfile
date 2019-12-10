# Docker Build Arguments
ARG FROM_IMAGE
FROM $FROM_IMAGE


RUN setproxy && \
    curl -OL https://bootstrap.pypa.io/get-pip.py && \
    python get-pip.py && \
    rm get-pip.py && \
    unsetproxy

CMD ["python"]
