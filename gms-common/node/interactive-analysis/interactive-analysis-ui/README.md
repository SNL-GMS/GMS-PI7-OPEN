This project consists of multiple sub-projects connected using [lerna](https://github.com/lerna/lerna).

## Installation

* Install [Nodejs v8](https://nodejs.org/en/download/)

Then,
```bash
[ .../interactive_analysis_ui] $ npm install
[ .../interactive_analysis_ui] $ npm run bootstrap
[ .../interactive_analysis_ui] $ npm run build
```

## Deployment

This directory contains a `Dockerfile` and can be built as such, e.g. `docker build -t gms/analyst-ui .`

## Development

After installing dependencies, see the README in any sub-project under [./packages](packages) for instructions on developing in that particular project

## Packages

[analyst-ui-core](./packages/analyst-ui-core)

[analyst-ui-electron](./packages/analyst-ui-electron)

[ui-core-components](./packages/ui-core-components)

[weavess](./packages/weavess)