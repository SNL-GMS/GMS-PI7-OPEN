/* tslint:disable */
const dir = require('./webpack-paths').dir;
const webpack = require('webpack');
const path = require('path');

// plugins
const HtmlWebpackPlugin = require('html-webpack-plugin');
const CaseSensitivePathsPlugin = require('case-sensitive-paths-webpack-plugin');
const ForkTsCheckerWebpackPlugin = require('fork-ts-checker-webpack-plugin');

const version = require(path.resolve(dir.root, 'package.json')).version;

module.exports = {
    entry: {
        app: path.resolve(dir.src, 'ts/examples/index.tsx'),
    },
    target: "web",
    output: {
        filename: '[name].js'
    },
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
            },
            {
                oneOf: [{
                        loader: 'url-loader',
                        options: {
                            limit: 100000
                        },
                        test: /\.(png|woff|woff2|eot|ttf|svg)$/
                    },
                    {
                        test: /\.css$/,
                        use: [
                            'style-loader',
                            {
                                loader: 'css-loader',
                                options: {
                                    minimize: true
                                }
                            }
                        ]
                    },
                    {
                        test: /\.scss$/,
                        use: ['style-loader', 'css-loader', 'resolve-url-loader', 'sass-loader?sourceMap']
                    }
                ]
            }
        ],
    },
    plugins: [
        new CaseSensitivePathsPlugin(),

        // normal app stuff
        new HtmlWebpackPlugin({
            template: path.resolve(dir.root, 'index.html'),
            title: `Weavess [${version}]`,
        }),

        // add needed variables
        new webpack.DefinePlugin({
            __VERSION__: JSON.stringify(version)
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
    devServer: {
        host: '0.0.0.0',
        port: 8080,
        disableHostCheck: true,
        clientLogLevel: "none",
        overlay: {
            warnings: false,
            errors: true,
        }
    }
};