/* tslint:disable */
const webpack = require('webpack');

const NODE_CONFIG_ENV = process.env.NODE_CONFIG_ENV || 'default'; 

module.exports = {
  mode: 'development',
  devtool: 'inline-source-map',
  module: {
    rules: [
      {
          test: /\.js$/,
          use: ["source-map-loader"],
          enforce: "pre"
      },
    ],
  },
  plugins: [
    new webpack.DefinePlugin({
      'process.env': {
        NODE_ENV: JSON.stringify('development'),
        NODE_CONFIG_ENV: JSON.stringify(NODE_CONFIG_ENV),
      },
    }),
  ]
};