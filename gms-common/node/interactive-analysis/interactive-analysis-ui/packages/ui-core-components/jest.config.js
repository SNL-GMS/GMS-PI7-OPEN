const { resolve } = require('path');
const root = resolve(__dirname, '.');

module.exports = {
  rootDir: root,
  verbose: true,
  name: "ui-core-components",
  verbose: true,
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
    "^react$": "<rootDir>/node_modules/react",
    "^react-dom$": "<rootDir>/node_modules/react-dom",
    ".*\\.(css|less|styl|scss|sass)$": "<rootDir>/__mocks__/styleMock.ts",
    ".*\\.(jpg|jpeg|png|gif|eot|otf|webp|svg|ttf|woff|woff2|mp4|webm|wav|mp3|m4a|aac|oga)$": "<rootDir>/__mocks__/fileMock.ts"
  },
  testEnvironment: "node",
  collectCoverage: true,
  coverageReporters: [
    "lcov",
    "html"
  ]
}