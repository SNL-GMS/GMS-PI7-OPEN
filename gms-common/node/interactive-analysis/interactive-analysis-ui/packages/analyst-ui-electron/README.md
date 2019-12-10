Electron code for window management/layout persistence 

## Running

By default, this will start an electron application and connect to `http://localhost:8080`, unless a `SERVER_URL` environment variable is specified

```bash
[.../analyst-ui-electron] $ npm start
```

or, to connect to a different url

```bash
[.../analyst-ui-electron] $ SERVER_URL=http://otherdomain.com:8080 npm start
```

## Generating Binaries

Generate binaries (on mac os, wine will need to be installed first. This can be done using `brew install wine`)

Set the `SERVER_URL` environment variable to set the default backend that the electron app will attempt to connect to. Otherwise, the url will be set to a development default (localhost)

```bash
[.../analyst-ui-electron] $ SERVER_URL=http://otherdomain.com:8080 npm run generate-bin
```

Binaries for darwin (mac os) and windows (win32) will be generated under `dist/`
