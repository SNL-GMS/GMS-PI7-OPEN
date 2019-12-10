const webpack = require('webpack');
const path = require('path');

const dir = {
    root: path.resolve(__dirname, '..'),
    nodeModules: path.resolve(__dirname, '../node_modules/'),
    src: path.resolve(__dirname, '../src'),
    dist: path.resolve(__dirname, '../dist'),
};

module.exports = {
    dir: dir
};