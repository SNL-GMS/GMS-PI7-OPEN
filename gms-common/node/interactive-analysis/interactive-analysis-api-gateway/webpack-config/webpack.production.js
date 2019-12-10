/* tslint:disable */
const webpack = require('webpack');
const UglifyJSPlugin = require('uglifyjs-webpack-plugin');

const NODE_CONFIG_ENV = process.env.NODE_CONFIG_ENV || 'default'; 

module.exports = {
  mode: 'production',
  stats: {
    colors: false,
    hash: true,
    timings: true,
    assets: true,
    chunks: true,
    chunkModules: true,
    modules: true,
    children: true,
  },
  optimization: {
    minimizer: [
      new UglifyJSPlugin({
        extractComments: true,
        cache: true,
        parallel: true,
        sourceMap: false,
        uglifyOptions: {
          compress: {
            inline: false
          }
        }
      })
    ],
    runtimeChunk: false,
  },
  plugins: [
    new webpack.DefinePlugin({
      'process.env': {
        NODE_ENV: JSON.stringify('production'),
        NODE_CONFIG_ENV: JSON.stringify(NODE_CONFIG_ENV),
      },
    }),
  ],
};