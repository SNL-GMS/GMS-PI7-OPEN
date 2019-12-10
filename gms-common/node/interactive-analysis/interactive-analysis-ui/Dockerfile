#
# Dockerfile for running an NGINX instance serving up the GMS analyst user interface.
#

# Start with the project NGINX base image
ARG DOCKER_REGISTRY
FROM $DOCKER_REGISTRY/gms/centos7/nginx:latest

# Install the gettext package for the envsubst command used in the CMD
USER root:root
RUN setproxy && \
    yum -y install gettext && \
    unsetproxy

# Copy files into the container
COPY ./packages/analyst-ui-core/nginx/ /etc/nginx
COPY ./docker-entrypoint.sh /docker-entrypoint.sh

# Create a directory for the interactive analysis UI
# and copy the UI files into the container
WORKDIR /opt/interactive-analysis-ui/

COPY ./packages/analyst-ui-core/build/ ./

# Since we're running as non-root, change the owner of places where NGINX writes
RUN chown nginx:root /opt/interactive-analysis-ui -R && chmod -R g+w /opt/interactive-analysis-ui && \
    chown nginx:root /etc/nginx/ -R && chmod -R g+w /etc/nginx/ && \
    chown nginx:root /var/cache/nginx/ -R && chmod -R g+w /var/cache/nginx/ && \
    chown nginx:root /var/log/nginx/ -R && chmod -R g+w /var/log/nginx/ && \
    chown nginx:root /docker-entrypoint.sh && chmod -R g+w /docker-entrypoint.sh

# Set the user to the nginx user
USER nginx:root

# Expose NGINX ports
EXPOSE 8080 8443

# Set the run command to:
# 1. Create the ngninx conf file based on a template file and environment variables
# 2. Run nginx in the foreground ('deamon off')
ENTRYPOINT ["/bin/bash", "-c", "/docker-entrypoint.sh"]
