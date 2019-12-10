/* tslint:disable */
const path = require('path');
const dir = require('./webpack-paths').dir;
const webpack = require('webpack');

const CopyWebpackPlugin = require('copy-webpack-plugin');

module.exports = {
  mode: 'development',
  devtool: 'inline-cheap-module-source-map',
  module: {
    rules: [
      {
        oneOf: [{
            test: /\.(png|gif|jpg|jpeg|svg|xml|woff|woff2|eot|ttf)$/i,
            use: [{
              loader: 'url-loader',
              options: {
                limit: 100000
              },
            }, ]
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
                },
                'resolve-url-loader'
            ]
          },
          {
            test: /\.(sass|scss)$/,
            use: [
              {
                loader: 'style-loader',
                options: {
                  hmr: true,
                  sourceMap: true,
                  convertToAbsoluteUrls: true
                }
              },
              {
                loader: 'css-loader',
                options: {
                  minimize: true
                },
              },
              'resolve-url-loader',
              {
                loader: 'sass-loader',
                options: {
                  sourceMap: true,
                  sourceMapContents: false
                }
              }
            ]
          },
          {
            loader: 'script-loader',
            test: /cesium\.js$/
          }
        ]
      },
      {
          test: /\.js$/,
          use: ["source-map-loader"],
          enforce: "pre",
          exclude: [
            // these packages have problems with their sourcemaps
            /node_modules\/apollo-client/,
            /node_modules\/subscriptions-transport-ws/,
            /node_modules\/apollo-cache-hermes/,
            /node_modules\/cesium/

          ]
      },
    ],
  },
  plugins: [

    // Copy Cesium Assets, Widgets, and Workers to a static directory
    new CopyWebpackPlugin([
      {
        from: dir.cesiumBuildUnminified,
        to: path.join(dir.build, 'cesium')
      }
    ]),

    new webpack.DefinePlugin({
      'process.env': {
        NODE_ENV: JSON.stringify('development')
      },
    }),
  ]
};