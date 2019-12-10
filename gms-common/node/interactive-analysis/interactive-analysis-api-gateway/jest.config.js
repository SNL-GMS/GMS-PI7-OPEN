const { resolve } = require('path');
const root = resolve(__dirname, '.');

module.exports = {
  rootDir: root,
  verbose: true,
  name: "interactive-analysis-api-gateway",
  testURL: "http://localhost/",
  transform: {
    "^.+\\.jsx?$": "babel-jest",
    "^.+\\.tsx?$": "ts-jest",
  },
  testRegex: "/__tests__/.*\\.(ts|tsx)$",
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