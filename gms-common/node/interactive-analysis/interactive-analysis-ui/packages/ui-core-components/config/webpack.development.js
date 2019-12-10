/* tslint:disable */
const dir = require('./webpack-paths').dir;
const webpack = require('webpack');

module.exports = {
  mode: 'development',
  devtool: 'inline-cheap-module-source-map',
  plugins: [
    new webpack.DefinePlugin({
      'process.env': {
        NODE_ENV: JSON.stringify('development')
      }
    }),
  ]
};