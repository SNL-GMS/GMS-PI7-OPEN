/* tslint:disable */
const dir = require('./webpack-paths').dir;
const webpack = require('webpack');
const path = require('path');

// plugins
const HtmlWebpackPlugin = require('html-webpack-plugin');
const CaseSensitivePathsPlugin = require('case-sensitive-paths-webpack-plugin');
const ForkTsCheckerWebpackPlugin = require('fork-ts-checker-webpack-plugin');
const TsconfigPathsPlugin = require('tsconfig-paths-webpack-plugin');
const CircularDependencyPlugin = require("circular-dependency-plugin");

const version = require(path.resolve(dir.root, 'package.json')).version;

// default development values
var graphql_proxy_uri = process.env.GRAPHQL_PROXY_URI || 'http://localhost:3000';
var waveforms_proxy_uri = process.env.WAVEFORMS_PROXY_URI || 'http://localhost:3000';
var subscriptions_proxy_uri = process.env.SUBSCRIPTIONS_PROXY_URI || 'ws://localhost:4000';

// Explanations:
// https://cesiumjs.org/tutorials/cesium-and-webpack/

module.exports = {
  entry: {
    'analyst-ui-core': path.resolve(dir.src, 'ts/index.tsx'),
  },
  output: {
    filename: 'analyst-ui-core.js',
    chunkFilename: '[name].[contenthash:8].js',
    path: dir.build,
    // needed to compile multiline strings in Cesium
    sourcePrefix: ''
  },
  target: "web",
  amd: {
    // enable webpack-friendly use of require in Cesium
    toUrlUndefined: true
  },
  node: {
    // Resolve node module use of fs
    fs: 'empty'
  },
  module: {
    strictExportPresence: true,
    unknownContextCritical: false,
    rules: [{
        exclude: dir.nodeModules,
        test: /\.ts(x?)$/,
        use: [{
          loader: 'ts-loader',
          options: {
            transpileOnly: true
          }
        }]
      },
    ]
  },
  plugins: [
    new webpack.HashedModuleIdsPlugin(), // so that file hashes don't change unexpectedly

    new CaseSensitivePathsPlugin(),

    // Zero tolereance for circular depenendencies
    new CircularDependencyPlugin({
      exclude: /.js|node_modules/,
      failOnError: true,
    }),

    // normal app stuff
    new HtmlWebpackPlugin({
      template: path.resolve(dir.root, 'index.html'),
      title: `Interactive Analysis [${version}]`,
      favicon: path.resolve(dir.root, '../analyst-ui-electron/gms-logo.ico')
    }),

    // add needed variables
    new webpack.DefinePlugin({
      __VERSION__: JSON.stringify(version),
      CESIUM_BASE_URL: JSON.stringify('build/cesium/')
    }),

    // faster rebuilds
    new webpack.IgnorePlugin(/^\.\/locale$/, /moment$/),

    // Issue with tslint: no-unused-variable
    // https://github.com/Realytics/fork-ts-checker-webpack-plugin/issues/74
    // https://github.com/palantir/tslint/pull/2763
    // https://github.com/palantir/tslint/issues/1481
    new ForkTsCheckerWebpackPlugin({
        tslint: path.resolve(dir.root, 'tslint.json'),
        tsconfig: path.resolve(dir.root, 'tsconfig.json'),
        watch: dir.src,
        tslintAutoFix: true,
        memoryLimit: 8192,
        async: true
    }),

    new webpack.DefinePlugin({
      'process.env': {
        CESIUM_OFFLINE: JSON.stringify(process.env.CESIUM_OFFLINE)
      },
    }),
  ],
  resolve: {
    extensions: ['.json', '.scss', '.eot', '.css', '.jsx', '.js', '.ts', '.tsx', '.js.map'],
    alias: {
      cesium: dir.cesiumSource,
    },
    plugins: [
      new TsconfigPathsPlugin({ configFile: path.resolve(dir.root, 'tsconfig.json')  })
    ]
  },
  externals: {
    electron: 'electron'
  },
  devServer: {
    host: "0.0.0.0",
    port: 8080,
    compress: true,
    disableHostCheck: false,
    clientLogLevel: "none",
    overlay: {
      warnings: false,
      errors: true,
    },
    proxy: {
      "/graphql": {
        target: graphql_proxy_uri,
        secure: false,
        changeOrigin: true,
        logLevel: "warn",

      },
      "/waveforms": {
        target: waveforms_proxy_uri,
        secure: false,
        changeOrigin: true,
        logLevel: "warn",
      },
      "/subscriptions": {
        target: subscriptions_proxy_uri,
        secure: false,
        ws: true,
        logLevel: "warn",
      }
    },
  },
};