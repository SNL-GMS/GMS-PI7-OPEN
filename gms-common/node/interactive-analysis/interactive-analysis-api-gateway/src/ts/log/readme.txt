the tools identified issues in the gateway-logger file
with an example of creating an http transport at line 68

new (transports.Http)({
            The Http transport is a generic way to log, query, and stream logs
            from an arbitrary Http endpoint, preferably winstond.
            It takes options that are passed to the node.js http or https request:
            @host: (Default: localhost) Remote host of the HTTP logging endpoint
            @port: (Default: 80 or 443) Remote port of the HTTP logging endpoint
            @path: (Default: /) Remote URI of the HTTP logging endpoint
            @auth: (Default: None) An object representing the username and password for HTTP Basic Auth
            @ssl: (Default: false) Value indicating if we should us HTTPS
      host: 'host',
      port: 9999,
      path: '/',
      auth: someObject,
      ssl: false
    })
