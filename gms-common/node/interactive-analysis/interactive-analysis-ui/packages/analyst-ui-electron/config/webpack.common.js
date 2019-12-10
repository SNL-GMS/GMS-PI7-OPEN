/* tslint:disable */
const dir = require('./webpack-paths').dir;
const webpack = require('webpack');
const path = require('path');

// plugins
const CaseSensitivePathsPlugin = require('case-sensitive-paths-webpack-plugin');
const ForkTsCheckerWebpackPlugin = require('fork-ts-checker-webpack-plugin');

const version = require(path.resolve(dir.root, 'package.json')).version;

const NODE_ENV = process.env.NODE_ENV || 'development';
const SERVER_URL = process.env.SERVER_URL || 'http://localhost:8080';

module.exports = {
    entry: {
        app: "./src/ts/index.ts",
    },
    output: {
        filename: "../build/analyst-ui-electron.js",
    },
    target: "electron-main",
    module: {
        rules: [{
            exclude: dir.nodeModules,
            test: /\.ts(x?)$/,
            use: [{
                loader: 'ts-loader',
                options: {
                    transpileOnly: true
                }
            }]
        }],
    },
    plugins: [
        new CaseSensitivePathsPlugin(),

        new webpack.DefinePlugin({}),

        // add needed variables
        new webpack.DefinePlugin({
            __VERSION__: JSON.stringify(version),
            'DEFAULT_SERVER_URL': JSON.stringify(SERVER_URL),
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
            watch: dir.src
        }),
    ],

    resolve: {
        extensions: [".webpack.js", ".web.js", ".ts", ".tsx", ".js", ".scss", ".css"],
    },
};