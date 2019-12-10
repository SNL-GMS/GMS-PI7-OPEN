const path = require('path');
const webpack = require('webpack');
const nodeExternals = require('webpack-node-externals');

const dir = {
    root: path.resolve(__dirname, '..'),
    build: path.resolve(__dirname, '../build'),
};

module.exports = {
    entry: {
        app: "./src/ts/server.ts",
    },
    target: "node",
    module: {
        rules: [
            {
                exclude: path.resolve(__dirname, '../node_modules/'),
                test: /\.ts(x?)$/,
                use: [{
                    loader: 'ts-loader',
                    options: {
                        transpileOnly: true
                    }
                }]
            },
            {
                test: /\.mjs$/,
                type: 'javascript/auto',
            },
        ],
    },
    output: {
        filename: 'api-gateway.bundle.js',
        path: dir.build,
        sourcePrefix: ''
      },
    resolve: {
        extensions: [".webpack.js", ".web.js", ".ts", ".tsx", ".js", ".scss", ".css"],
    },
    plugins: [
        new webpack.DefinePlugin({ CONFIG: JSON.stringify(require("config")) }),
        new webpack.IgnorePlugin(/^encoding$/, /node-fetch/),
    ],
    externals: [nodeExternals()], // in order to ignore all modules in node_modules folder,
};