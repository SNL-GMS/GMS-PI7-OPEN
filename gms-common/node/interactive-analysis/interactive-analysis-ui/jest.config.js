const { resolve } = require('path');
const root = resolve(__dirname, '.');

module.exports = {
  rootDir: root,
  verbose: true,
  name: "interactive-analysis",
  testURL: "http://localhost/",
  setupFiles: [
    "<rootDir>/jest.setup.ts"
  ],
  roots: [
    "<rootDir>/packages/analyst-ui-core",
    "<rootDir>/packages/analyst-ui-electron",
    "<rootDir>/packages/ui-core-components",
    "<rootDir>/packages/weavess"
  ],
  projects: [
    "<rootDir>/packages/analyst-ui-core/jest.config.js",
    "<rootDir>/packages/analyst-ui-electron/jest.config.js",
    "<rootDir>/packages/ui-core-components/jest.config.js",
    "<rootDir>/packages/weavess/jest.config.js"
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
  testEnvironment: "node",
  collectCoverage: true,
  coverageReporters: [
    "lcov",
    "html"
  ]
}