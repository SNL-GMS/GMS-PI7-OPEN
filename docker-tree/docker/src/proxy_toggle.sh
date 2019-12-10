# No Proxy
unsetproxy () {
    unset http_proxy https_proxy HTTP_PROXY HTTPS_PROXY no_proxy NO_PROXY
}

# Proxy
setproxy () {

    # ENV_PROXY is set in the Dockerfile
    http_proxy=${ENV_PROXY}
    HTTP_PROXY=$http_proxy
    https_proxy=$http_proxy
    HTTPS_PROXY=$https_proxy

    # ENV_NO_PROXY is set in the Dockerfile
    no_proxy=${ENV_NO_PROXY}
    NO_PROXY=$no_proxy
    export http_proxy https_proxy HTTP_PROXY HTTPS_PROXY no_proxy NO_PROXY
}
