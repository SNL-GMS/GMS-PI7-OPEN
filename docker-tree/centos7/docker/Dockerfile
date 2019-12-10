# Docker Build Arguments
ARG FROM_IMAGE
FROM $FROM_IMAGE

RUN setproxy && \
    yum update -y && \
    yum install -y docker-client && \
    unsetproxy

CMD ["/bin/bash"]
