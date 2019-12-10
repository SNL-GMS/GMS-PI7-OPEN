FROM swaggerapi/swagger-ui

ENV VALIDATOR_URL="" \
    SWAGGER_JSON=/usr/share/swagger.json

COPY swagger.json ${SWAGGER_JSON}

RUN chgrp -R 0 /usr/share/nginx /etc/nginx /var/cache/nginx /var/run ${SWAGGER_JSON} && \
    chmod -R g=u /usr/share/nginx /etc/nginx /var/cache/nginx /var/run ${SWAGGER_JSON}

CMD ["sh", "/usr/share/nginx/run.sh"]
