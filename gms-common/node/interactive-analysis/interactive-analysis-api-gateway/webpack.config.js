/* tslint:disable */
const merge = require('webpack-merge');
const common = require('./webpack-config/webpack.common');

// What's this webpack-merge thing? Read here: https://survivejs.com/webpack/developing/composing-configuration/
module.exports = mode => {
  if (mode === 'production') {
    const productionConfig = require('./webpack-config/webpack.production');
    return merge(common, productionConfig, {
      mode
    });
  }

  const developmentConfig = require('./webpack-config/webpack.development');
  return merge(common, developmentConfig, {
    mode
  });

};