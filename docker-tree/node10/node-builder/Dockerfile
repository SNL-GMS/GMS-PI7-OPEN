# Docker Build Arguments
ARG FROM_IMAGE
FROM $FROM_IMAGE

ENV NODEJS_VERSION=10 \
    NPM_RUN=start \
    NAME=nodejs \
    NPM_CONFIG_PREFIX=$HOME/.npm-global \
    PATH=$HOME/node_modules/.bin/:$HOME/.npm-global/bin/:$PATH

# Copy and install node packages and remove package.json after
COPY src/package.json .

RUN setproxy && \
    npm install --global && \
    rm package.json && \
    apt-get update -y && \
    apt-get install software-properties-common python3-pip -y && \
    python3 -m pip install Jinja2 && \
    unsetproxy && \
    apt-get clean

CMD ["npm"]
