# Docker Build Arguments
ARG FROM_IMAGE
FROM $FROM_IMAGE


# Configure an nginx repo and install
ADD src/nginx.repo /etc/yum.repos.d/
RUN cd /tmp && \
    setproxy && \
    curl -O http://nginx.org/keys/nginx_signing.key && \
    rpm --import nginx_signing.key && \
    yum update -y && \
    yum install -y nginx && \
    unsetproxy && \
    yum clean all && rm -rf /tmp/nginx*

# Since we're running as non-root, change the owner of places
# where nginx writes
RUN chown nginx:nginx /var/cache/nginx/ -R && \
    chown nginx:nginx /var/log/nginx/ -R

# Expose NGINX ports
EXPOSE 8080 8443

USER nginx

ENTRYPOINT ["nginx", "-g", "daemon off;"]
