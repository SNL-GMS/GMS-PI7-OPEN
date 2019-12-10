/* tslint:disable */
const path = require('path');
const dir = require('./webpack-paths').dir;
const webpack = require('webpack');

const CopyWebpackPlugin = require('copy-webpack-plugin');
const UglifyJSPlugin = require('uglifyjs-webpack-plugin');
const MiniCssExtractPlugin = require("mini-css-extract-plugin");
const OptimizeCSSAssetsPlugin = require("optimize-css-assets-webpack-plugin");

// Explanations:
// https://webpack.js.org/plugins/split-chunks-plugin/
// https://hackernoon.com/the-100-correct-way-to-split-your-chunks-with-webpack-f8a9df5b7758
// https://hackernoon.com/optimising-your-application-bundle-size-with-webpack-e85b00bab579
// https://itnext.io/react-router-and-webpack-v4-code-splitting-using-splitchunksplugin-f0a48f110312

module.exports = {
  mode: 'production',
  devtool: 'none',
  module: {
    rules: [
      {
        test: /\.js$/,
        enforce: 'pre',
        use: [{
          loader: 'strip-pragma-loader',
          options: {
            pragmas: {
              debug: false
            }
          }
        }]
      },
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
                MiniCssExtractPlugin.loader,
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
                  hmr: false,
                  sourceMap: false,
                  convertToAbsoluteUrls: false
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
      }
    ],
  },
  performance: {
    hints: false
  },
  stats: {
    colors: true,
    hash: true,
    timings: true,
    assets: true,
    chunks: true,
    chunkModules: true,
    modules: true,
    children: false,
  },
  optimization: {
    runtimeChunk: 'single',
    minimize: true,
    minimizer: [
      new UglifyJSPlugin({
        cache: false,
        parallel: true,
        sourceMap: false,
        uglifyOptions: {
          warning: false,
          compress: true,
          output: {
            comments: false
          },
        },
        extractComments: 'all',        
        exclude: [/\.min\.js$/gi] // skip pre-minified libs
      }),

      new OptimizeCSSAssetsPlugin({})

    ],
    splitChunks: {
      chunks: 'all',
      maxInitialRequests: Infinity,
      minSize: 0,
      cacheGroups: {
        vendor: {
          test: /[\\/]node_modules[\\/]/,
          reuseExistingChunk: true,
          name(module) {
            // get the name. E.g. node_modules/packageName/not/this/part.js
            // or node_modules/packageName
            const packageName = module.context.match(/[\\/]node_modules[\\/](.*?)([\\/]|$)/)[1];

            // npm package names are URL-safe, but some servers don't like @ symbols
            return `npm.${packageName.replace('@', '')}`;
          },
        }
      },
    },
  },
  plugins: [

    // Some loaders accept configuration through webpack internals
    new webpack.LoaderOptionsPlugin({
      debug: false,
      minimize: true,
    }),

    new MiniCssExtractPlugin({
      filename: '[name].[hash].css',
      chunkFilename: '[id].[hash].css'
    }),

    // Copy Cesium Assets, Widgets, and Workers to a static directory
    new CopyWebpackPlugin([
      {
        from: dir.cesiumBuild,
        to: path.join(dir.build, 'cesium')
      }
    ]),

    new webpack.DefinePlugin({
      'process.env': {
        NODE_ENV: JSON.stringify('production')
      },
    }),
  ],
};