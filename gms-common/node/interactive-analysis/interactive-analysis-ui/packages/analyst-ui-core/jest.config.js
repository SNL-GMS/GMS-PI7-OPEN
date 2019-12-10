const { resolve } = require('path');
const root = resolve(__dirname, '.');

module.exports = {
  rootDir: root,
  verbose: true,
  name: "analyst-ui-core",
  testURL: "http://localhost/",
  setupFiles: [
    "<rootDir>/jest.setup.ts"
  ],
  snapshotSerializers: [
    "enzyme-to-json/serializer"
  ],
  "transform": {
    "^.+\\.jsx?$": "babel-jest",
    "^.+\\.tsx?$": "ts-jest"
  },
  testRegex: "/__tests__/.*\\.(test|spec)\\.(ts|tsx)$",
  moduleFileExtensions: [
    "ts",
    "tsx",
    "js",
    "json"
  ],
  modulePaths: [
    "./node_modules"
  ],
  moduleDirectories: [
    "./node_modules"
  ],
  moduleNameMapper: {
    "~css/(.*)": "<rootDir>/src/css/$1",
    "~apollo/(.*)": "<rootDir>/src/ts/apollo/$1",
    "~graphql/(.*)": "<rootDir>/src/ts/graphql/$1",
    "~state/(.*)": "<rootDir>/src/ts/state/$1",
    "~util/(.*)": "<rootDir>/src/ts/util/$1",
    "~analyst-ui/(.*)": "<rootDir>/src/ts/workspaces/analyst-ui/$1",
    "~data-acquisition-ui/(.*)": "<rootDir>/src/ts/workspaces/data-acquisition-ui/$1",
    "^react$": "<rootDir>/node_modules/react",
    "^react-dom$": "<rootDir>/node_modules/react-dom",
    "worker-loader": "<rootDir>/../weavess/node_modules/worker-loader",
    "loader-worker-loader$": "<rootDir>/../analyst-ui-core/node_modules/@gms/weavess/dist/lib/components/waveform-display/index.js",
    ".*\\.(css|less|styl|scss|sass)$": "<rootDir>/__mocks__/style-mock.ts",
    ".*\\.(jpg|jpeg|png|gif|eot|otf|webp|svg|ttf|woff|woff2|mp4|webm|wav|mp3|m4a|aac|oga)$": "<rootDir>/__mocks__/file-mock.ts"
  },
  testEnvironment: "node",
  collectCoverage: true,
  coverageReporters: [
    "lcov",
    "html"
  ]
}