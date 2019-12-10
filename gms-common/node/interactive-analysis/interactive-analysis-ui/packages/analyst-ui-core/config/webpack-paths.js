const webpack = require('webpack');
const path = require('path');

const dir = {
    root: path.resolve(__dirname, '..'),
    nodeModules: path.resolve(__dirname, '../node_modules/'),
    cesiumSource: path.resolve(__dirname, '../node_modules/cesium/Source/'),
    cesiumBuild: path.resolve(__dirname, '../node_modules/cesium/Build/Cesium/'),
    cesiumBuildUnminified: path.resolve(__dirname, '../node_modules/cesium/Build/CesiumUnminified/'),
    src: path.resolve(__dirname, '../src'),
    build: path.resolve(__dirname, '../build'),
};

module.exports = {
    dir: dir
};